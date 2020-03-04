package com.hedera.hcsapp.integration;

/*-
 * ‌
 * hcs-sxc-java
 * ​
 * Copyright (C) 2019 - 2020 Hedera Hashgraph, LLC
 * ​
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ‍
 */

import java.util.concurrent.ExecutionException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.messaging.simp.stomp.StompSession;
import org.springframework.messaging.simp.stomp.StompSessionHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.client.WebSocketClient;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;

import com.hedera.hcs.sxc.callback.OnHCSMessageCallback;
import com.hedera.hcs.sxc.commonobjects.HCSResponse;
import com.hedera.hcs.sxc.interfaces.SxcPersistence;
import com.hedera.hcsapp.AppData;
import com.hedera.hcsapp.States;
import com.hedera.hcsapp.Statics;
import com.hedera.hcsapp.Utils;
import com.hedera.hcsapp.appconfig.AppClient;
import com.hedera.hcsapp.entities.Credit;
import com.hedera.hcsapp.entities.Settlement;
import com.hedera.hcsapp.entities.SettlementItem;
import com.hedera.hcsapp.entities.SettlementItemId;
import com.hedera.hcsapp.notifications.CustomStompSessionHandler;
import com.hedera.hcsapp.notifications.NotificationMessage;
import com.hedera.hcsapp.repository.CreditRepository;
import com.hedera.hcsapp.repository.SettlementItemRepository;
import com.hedera.hcsapp.repository.SettlementRepository;
import com.hedera.hcsapp.repository.Util;

import lombok.extern.log4j.Log4j2;
import proto.CreditBPM;
import proto.PaymentInitBPM;
import proto.PaymentSentBPM;
import proto.SettleCompleteBPM;
import proto.SettleInitBPM;
import proto.SettlePaidBPM;
import proto.SettleProposeBPM;
import proto.SettlementBPM;

@Log4j2
@Component
public class HCSIntegration {
    
    private AppData appData;
    @Autowired
    private HCSMessages hcsMessages;
    
    @Autowired
    private Util repositoryUtil;
            
    @Autowired
    private CreditRepository creditRepository;

    @Autowired
    private SettlementRepository settlementRepository;

    @Autowired
    private SettlementItemRepository settlementItemRepository;
    
    private StompSession stompSession;

    public HCSIntegration() throws Exception {
        this.appData = Statics.getAppData();
        // create a callback object to receive the message
        OnHCSMessageCallback onHCSMessageCallback = new OnHCSMessageCallback(this.appData.getHCSCore());
        onHCSMessageCallback.addObserver(hcsMessage -> {
            processHCSMessage(hcsMessage);
        });
    }

    public void processHCSMessage(HCSResponse hcsResponse) {
        try {
            //ApplicationMessage applicationMessage = ApplicationMessage.parseFrom(hcsResponse.getMessage());
            SettlementBPM settlementBPM = SettlementBPM.parseFrom(hcsResponse.getMessage());
            // (CREDIT_PENDING , r ,threadId ,credit) => (CREDIT_AWAIT_ACK ,r ,threadId , credit[threadId].txId=r.MessageId)
            String threadId = settlementBPM.getThreadID();
            if (settlementBPM.hasCredit()) {
                log.debug("settlementBPM.hasCredit()");
                String priorState = States.CREDIT_PROPOSED_PENDING.name();
                String nextState = States.CREDIT_PROPOSED.name();

                CreditBPM creditBPM = settlementBPM.getCredit();
                String payerName = creditBPM.getPayerName();
                String recipientName = creditBPM.getRecipientName();
                
                // update the credit state
                creditRepository.findById(threadId).ifPresentOrElse(
                        (credit) -> {
                            if (credit.getStatus().equals(priorState)) {
                                credit.setStatus(nextState);
                                credit.setApplicationMessageId(Utils.applicationMessageIdToString(hcsResponse.getApplicationMessageId()));
                                creditRepository.save(credit);
                            } else {
                                log.error("Credit status should be " + priorState + ", found : " + credit.getStatus());
                            }
                        },
                        () -> {
                            Credit credit = Utils.creditFromCreditBPM(creditBPM, threadId);
                            credit.setStatus(nextState);
                            credit.setApplicationMessageId(Utils.applicationMessageIdToString(hcsResponse.getApplicationMessageId()));
                            creditRepository.save(credit);
                            log.debug("Adding new credit to Database: " + threadId);
                        }
                );

                notify("credits", payerName, recipientName, threadId);
                
                if (settlementBPM.getAutomatic()) {
                    // automatic processing, send ACK if appropriate
                    if (creditBPM.getRecipientName().contentEquals(this.appData.getUserName())) {
                        // lets make sure credit was recorded in the first place
                        creditRepository.findById(threadId).ifPresent(
                            (credit) -> {
                                try {
                                    hcsMessages.creditAck(this.appData, threadId, true);
                                    notify("credits", payerName, recipientName, threadId);
                                } catch (Exception e) {
                                    log.error(e);
                                }
                    });
                    }
                }
            } else if (settlementBPM.hasCreditAck()) {
                log.debug("settlementBPM.hasCreditAck()");
                updateCredit(threadId, States.CREDIT_PROPOSED, States.CREDIT_AGREED);
            } else if (settlementBPM.hasSettlePropose()) {
                log.debug("settlementBPM.hasSettlePropose()");
                String nextState = States.SETTLE_PROPOSED.name();

                SettleProposeBPM settleProposeBPM = settlementBPM.getSettlePropose();
                String payerName = settleProposeBPM.getPayerName();
                String recipientName = settleProposeBPM.getRecipientName();
                // update the settlement state
                settlementRepository.findById(threadId).ifPresentOrElse(
                        (settlement) -> {
                            if (settlement.getStatus().equals(nextState + "_PENDING")) {
                                settlement.setStatus(nextState);
                                settlement.setApplicationMessageId(Utils.applicationMessageIdToString(hcsResponse.getApplicationMessageId()));
                                settlementRepository.save(settlement);
                                // update the credits too
                                updateCreditStateForSettlementItems(threadId, nextState);
                                try {
                                    notify("settlements", settlement.getPayerName(), settlement.getRecipientName(),threadId);
                                } catch (Exception e) {
                                    log.error(e);
                                }
                            } else {
                                log.error("Settlement status should be " + nextState + "_PENDING" + ", found : " + settlement.getStatus());
                            }
                        },
                        () -> {
                            Settlement settlement = Utils.settlementFromSettleProposeBPM(settleProposeBPM, threadId);
                            settlement.setStatus(nextState);
                            settlement.setApplicationMessageId(Utils.applicationMessageIdToString(hcsResponse.getApplicationMessageId()));
                            settlementRepository.save(settlement);
                            log.debug("Adding new settlement to Database: " + threadId);

                            for (String settleThreadId : settleProposeBPM.getThreadIDsList()) {
                                SettlementItem settlementItem = new SettlementItem();
                                settlementItem.setId(new SettlementItemId(settleThreadId, threadId));
                                settlementItemRepository.save(settlementItem);
                            }
                            // update the credits too
                            updateCreditStateForSettlementItems(threadId, nextState);
                            notify("settlements", settlement.getPayerName(), settlement.getRecipientName(),threadId);
                        }
                );
                notify("settlements", payerName, recipientName, threadId);

                if (settlementBPM.getAutomatic()) {
                    // automatic processing, send ACK if appropriate
                    if (settleProposeBPM.getRecipientName().contentEquals(this.appData.getUserName())) {
                        // lets make sure settlement was recorded in the first place
                        settlementRepository.findById(threadId).ifPresent(
                            (settlement) -> {
                                try {
                                    hcsMessages.settlementAck(this.appData, threadId, true);
                                    notify("settlements", payerName, recipientName, threadId);
                                } catch (Exception e) {
                                    log.error(e);
                                }
                    });
                    }
                }
                
            } else if (settlementBPM.hasSettleProposeAck()) {
                log.debug("settlementBPM.hasSettleProposeAck()");
                updateSettlement(threadId, States.SETTLE_PROPOSED, States.SETTLE_AGREED);
                String payerName = settlementBPM.getSettleProposeAck().getSettlePropose().getPayerName();
                String recipientName = settlementBPM.getSettleProposeAck().getSettlePropose().getRecipientName();
                
                if (settlementBPM.getAutomatic()) {
                    // automatic processing, send ACK if appropriate
                    settlementRepository.findById(threadId).ifPresent(
                        (settlement) -> {
                            try {
                                if (settlement.getPayerName().contentEquals(this.appData.getUserName())) {
                                    String additionalNotes = "Initialise Payment (automatic)";
                                    String paymentChannelName = "";
                                    
                                    for (AppClient appClient : this.appData.getAppClients()) {
                                        if (appClient.getRoles().contains("PAYCHANNEL")) {
                                            paymentChannelName = appClient.getClientName();
                                            break;
                                        }
                                    }
                                    hcsMessages.settlementInit(this.appData, threadId, true, additionalNotes, paymentChannelName);
                                    notify("settlements", payerName, recipientName, threadId);
                                }
                            } catch (Exception e) {
                                log.error(e);
                            }
                });
                }
            } else if (settlementBPM.hasSettleInit()) { // proposes a pay channel
                log.debug("settlementBPM.hasSettleInit()");
                String priorState = States.SETTLE_AGREED.name();
                String nextState = States.SETTLE_PAY_CHANNEL_PROPOSED.name();
                String payerName = settlementBPM.getSettleInit().getPayerName();
                String recipientName = settlementBPM.getSettleInit().getRecipientName();
                        
                SettleInitBPM settleInitBPM = settlementBPM.getSettleInit();
                // update the settlement state
                settlementRepository.findById(threadId).ifPresent(
                        (settlement) -> {
                            if ((settlement.getStatus().equals(nextState + "_PENDING")) || (settlement.getStatus().equals(priorState))) {
                                settlement.setStatus(nextState);
                                settlement.setApplicationMessageId(Utils.applicationMessageIdToString(hcsResponse.getApplicationMessageId()));
                                settlement.setAdditionalNotes(settleInitBPM.getAdditionalNotes());
                                settlement.setPaymentChannelName(settleInitBPM.getPaymentChannelName());
                                settlementRepository.save(settlement);
                                // update the credits too
                                updateCreditStateForSettlementItems(threadId, nextState);
                            } else {
                                log.error("Settlement status should be " + priorState + " or " + nextState + "_PENDING" + ", found : " + settlement.getStatus());
                            }
                        }
                );
                notify("settlements", payerName, recipientName, threadId);

                if (settlementBPM.getAutomatic()) {
                    // automatic processing, send ACK if appropriate
                    settlementRepository.findById(threadId).ifPresent(
                        (settlement) -> {
                            try {
                                if (settlement.getRecipientName().contentEquals(this.appData.getUserName())) {
                                    hcsMessages.settleProposeChannelAck(this.appData, threadId, true);
                                    notify("settlements", payerName, recipientName, threadId);
                                }
                            } catch (Exception e) {
                                log.error(e);
                            }
                });
                }
            } else if (settlementBPM.hasSettleInitAck()) { // agrees pay channel
                log.debug("settlementBPM.hasSettleInitAck()");
                updateSettlement(threadId, States.SETTLE_PAY_CHANNEL_PROPOSED, States.SETTLE_PAY_CHANNEL_AGREED);
                String payerName = settlementBPM.getSettleInitAck().getSettleInit().getPayerName();
                String recipientName = settlementBPM.getSettleInitAck().getSettleInit().getRecipientName();

                if (settlementBPM.getAutomatic()) {
                    // automatic processing, send ACK if appropriate
                    settlementRepository.findById(threadId).ifPresent(
                        (settlement) -> {
                            try {
                                if (settlement.getPayerName().contentEquals(this.appData.getUserName())) {
                                    String payerAccountDetails = "";
                                    String recipientAccountDetails = "";
                                    for (AppClient appClient : this.appData.getAppClients()) {
                                        if (appClient.getClientName().contentEquals(settlement.getPayerName())) {
                                            payerAccountDetails = appClient.getPaymentAccountDetails();
                                        }
                                        if (appClient.getClientName().contentEquals(settlement.getRecipientName())) {
                                            recipientAccountDetails = appClient.getPaymentAccountDetails();
                                        }
                                    }
                                    String additionalNotes = "Start payment (automatic)";
                                    hcsMessages.settlePaymentInit(this.appData, threadId, true, payerAccountDetails, recipientAccountDetails, additionalNotes);
                                    notify("settlements", payerName, recipientName, threadId);
                                }
                            } catch (Exception e) {
                                log.error(e);
                            }
                });
                }
            } else if (settlementBPM.hasPaymentInit()) {
                log.debug("settlementBPM.hasPaymentInit()");
                
                String priorState = States.SETTLE_PAY_CHANNEL_AGREED.name();
                String nextState = States.SETTLE_PAY_PROPOSED.name();

                String payerName = settlementBPM.getPaymentInit().getPayerName();
                String recipientName = settlementBPM.getPaymentInit().getRecipientName();
                notify("settlements", payerName, recipientName, threadId);

                PaymentInitBPM paymentInitBPM = settlementBPM.getPaymentInit();
                // update the settlement state
                settlementRepository.findById(threadId).ifPresent(
                        (settlement) -> {
                            // only update status if user is paychannel or initiator
                            if ((settlement.getPayerName().endsWith(this.appData.getUserName())) || (settlement.getPaymentChannelName().equals(this.appData.getUserName()))) {
                                if ((settlement.getStatus().equals(nextState + "_PENDING")) || (settlement.getStatus().equals(priorState))) {
                                    settlement.setStatus(nextState);
                                    settlement.setApplicationMessageId(Utils.applicationMessageIdToString(hcsResponse.getApplicationMessageId()));
                                    settlement.setAdditionalNotes(paymentInitBPM.getAdditionalNotes());
                                    settlement.setPayerAccountDetails(paymentInitBPM.getPayerAccountDetails());
                                    settlement.setRecipientAccountDetails(paymentInitBPM.getRecipientAccountDetails());
                                    
                                    settlementRepository.save(settlement);
                                    // update the credits too
                                    updateCreditStateForSettlementItems(threadId, nextState);
                                    notify("settlements", payerName, recipientName, threadId);
                                } else {
                                    log.error("Settlement status should be " + priorState + " or " + nextState + ", found : " + settlement.getStatus());
                                }
                            }
                        }
                );
                
                if (settlementBPM.getAutomatic()) {
                    // automatic processing, send ACK if appropriate
                    settlementRepository.findById(threadId).ifPresent(
                        (settlement) -> {
                            try {
                                if (settlement.getPaymentChannelName().contentEquals(this.appData.getUserName())) {
                                    hcsMessages.settlePaymentInitAck(this.appData, threadId, true);
                                    notify("settlements", payerName, recipientName, threadId);
                                }
                            } catch (Exception e) {
                                log.error(e);
                            }
                });
                }
            } else if (settlementBPM.hasPaymentInitAck()) {
                log.debug("settlementBPM.hasPaymentInitAck()");
                // Pay channel initiates payment (bank transfer between parties) and sends payment made message if
                // self is responsible for payment

                settlementRepository.findById(threadId).ifPresent(
                    (settlement) -> {
                        // only update status if user is paychannel or initiator
                        if ((settlement.getPayerName().equals(this.appData.getUserName())) || (settlement.getPaymentChannelName().equals(this.appData.getUserName()))) {
                            updateSettlement(threadId, States.SETTLE_PAY_PROPOSED, States.SETTLE_PAY_AGREED);
                        }

                        // only send payment made message if user is the payment channel
                        if (settlement.getPaymentChannelName().equals(this.appData.getUserName())) {

                            int random = (int) ((Math.random() * ((10000 - 1) + 1)) + 1);
                            String payref = String.format("PAYREF{%05d}",random);
                            
                            try {
                                hcsMessages.settlePaymentSent(this.appData, threadId, settlementBPM.getAutomatic(), payref);
                                notify("settlements", settlement.getPayerName(), settlement.getRecipientName(), threadId);
                            } catch (Exception e) {
                                log.error(e);
                            }
                        }
                    }
                );
            } else if (settlementBPM.hasPaymentSent()) {
                log.debug("settlementBPM.hasPaymentSent()");
                String priorState = States.SETTLE_PAY_AGREED.name();
                String nextState = States.SETTLE_PAY_MADE.name();

                PaymentSentBPM paymentSentBPM = settlementBPM.getPaymentSent();
                // update the settlement state
                settlementRepository.findById(threadId).ifPresent(
                        (settlement) -> {
                            // only update status if user is paychannel or initiator
                            if ((settlement.getPayerName().equals(this.appData.getUserName())) || (settlement.getPaymentChannelName().equals(this.appData.getUserName()))) {
                                if ((settlement.getStatus().equals(nextState + "_PENDING")) || (settlement.getStatus().equals(priorState))) {
                                    settlement.setStatus(nextState);
                                    settlement.setApplicationMessageId(Utils.applicationMessageIdToString(hcsResponse.getApplicationMessageId()));
                                    settlement.setAdditionalNotes(paymentSentBPM.getAdditionalNotes());
                                    settlement.setPaymentReference(paymentSentBPM.getPaymentReference());
                                    
                                    settlementRepository.save(settlement);
                                    // update the credits too
                                    updateCreditStateForSettlementItems(threadId, nextState);
                                    notify("settlements", settlement.getPayerName(), settlement.getRecipientName(),threadId);
                                    
                                    // only send next message if user is the originator
                                    if (settlement.getPayerName().equals(this.appData.getUserName())) {
                                        try {
                                            hcsMessages.settlePaymentSentAck(this.appData, threadId, settlementBPM.getAutomatic(), paymentSentBPM);
                                            notify("settlements", settlement.getPayerName(), settlement.getRecipientName(), threadId);
                                        } catch (Exception e) {
                                            log.error(e);
                                        }
                                    }
                                    
                                } else {
                                    log.error("Settlement status should be " + nextState + "_PENDING" + " or " + priorState + ", found : " + settlement.getStatus());
                                }
                            }
                        }
                );

            } else if (settlementBPM.hasPaymentSentAck()) {
                log.debug("settlementBPM.hasPaymentSentAck()");
                
                settlementRepository.findById(threadId).ifPresent(
                    (settlement) -> {
                        // only update status if user is paychannel or initiator
                        if ((settlement.getPayerName().equals(this.appData.getUserName())) || (settlement.getPaymentChannelName().equals(this.appData.getUserName()))) {
                            updateSettlement(threadId, States.SETTLE_PAY_MADE, States.SETTLE_PAY_ACK);
                        }
                        if (settlementBPM.getAutomatic()) {
                            if (settlement.getPayerName().equals(this.appData.getUserName())) {
                                try {
                                    String additionalNotes = "Settlement paid (automatic)";
                                    hcsMessages.settlePaymentPaid(this.appData, threadId, settlementBPM.getAutomatic(), additionalNotes);
                                    notify("settlements", settlement.getPayerName(), settlement.getRecipientName(), threadId);
                                } catch (Exception e) {
                                    log.error(e);
                                }
                            }
                        }                        
                    }
                );
                
            } else if (settlementBPM.hasSettlePayment()) {
                log.debug("settlementBPM.hasSettlePayment()");
                String priorState = States.SETTLE_PAY_ACK.name();
                String nextState = States.SETTLE_RCPT_REQUESTED.name();

                SettlePaidBPM settlePaidBPM = settlementBPM.getSettlePayment();
                // update the settlement state
                settlementRepository.findById(threadId).ifPresent(
                        (settlement) -> {
                            // only update state if payer or recipient
                            if ((settlement.getPayerName().equals(this.appData.getUserName())) || (settlement.getRecipientName().equals(this.appData.getUserName()))) {
                                boolean doUpdate = false;
                                if ((settlement.getStatus().equals(nextState + "_PENDING")) || (settlement.getStatus().equals(priorState))) {
                                    // payer settlement status is ok
                                    doUpdate = true;
                                }
                                if (settlement.getStatus().equals(States.SETTLE_PAY_CHANNEL_AGREED.name())) {
                                    // recipient settlement status is ok
                                    doUpdate = true;
                                }
                                if (doUpdate) {
                                    settlement.setStatus(nextState);
                                    settlement.setApplicationMessageId(Utils.applicationMessageIdToString(hcsResponse.getApplicationMessageId()));
                                    settlement.setAdditionalNotes(settlePaidBPM.getAdditionalNotes());
                                    settlement.setPaymentReference(settlePaidBPM.getPaymentReference());
                                    
                                    settlementRepository.save(settlement);
                                    // update the credits too
                                    updateCreditStateForSettlementItems(threadId, nextState);
                                    notify("settlements", settlement.getPayerName(), settlement.getRecipientName(),threadId);
                                } else {
                                    log.error("Settlement status should be " + nextState + "_PENDING" + " or " + priorState + ", found : " + settlement.getStatus());
                                }
                            }
                            if (settlementBPM.getAutomatic()) {
                                if (settlement.getRecipientName().equals(this.appData.getUserName())) {
                                    try {
                                        hcsMessages.settlePaymentPaidAck(this.appData, threadId, settlementBPM.getAutomatic());
                                        notify("settlements", settlement.getPayerName(), settlement.getRecipientName(), threadId);
                                    } catch (Exception e) {
                                        log.error(e);
                                    }
                                }
                            }                        
                        }
                );
            } else if (settlementBPM.hasSettlePaymentAck()) {
                log.debug("settlementBPM.hasSettlePaymentAck()");
                settlementRepository.findById(threadId).ifPresent(
                        (settlement) -> {
                            // only update state if payer or recipient
                            if (settlement.getPayerName().equals(this.appData.getUserName())) {
                                updateSettlement(threadId, States.SETTLE_RCPT_REQUESTED, States.SETTLE_RCPT_CONFIRMED);
                                if (settlementBPM.getAutomatic()) {
                                    try {
                                        String additionalNotes = "Payment complete ? (automatic)";
                                        hcsMessages.settlePaymentComplete(this.appData, threadId, settlementBPM.getAutomatic(), additionalNotes);
                                        notify("settlements", settlement.getPayerName(), settlement.getRecipientName(), threadId);
                                    } catch (Exception e) {
                                        log.error(e);
                                    }
                                }
                            } else if (settlement.getRecipientName().equals(this.appData.getUserName())) {
                                settlement.setStatus(States.SETTLE_COMPLETE.name());
                                settlementRepository.save(settlement);
                                // update the credits too
                                updateCreditStateForSettlementItems(threadId, States.SETTLE_COMPLETE.name());
                                notify("settlements", settlement.getPayerName(), settlement.getRecipientName(),threadId);
                            }
                        }
                    );
            } else if (settlementBPM.hasSettleComplete()) {
                log.debug("settlementBPM.hasSettleComplete()");
                String priorState = States.SETTLE_RCPT_CONFIRMED.name();
                String nextState = States.SETTLE_PAY_CONFIRMED.name();

                SettleCompleteBPM settleCompleteBPM = settlementBPM.getSettleComplete();
                // update the settlement state
                settlementRepository.findById(threadId).ifPresent(
                        (settlement) -> {
                            if (settlement.getPayerName().equals(this.appData.getUserName())) {
                                // payer updates status to SETTLE_PAY_CONFIRMED
                                if ((settlement.getStatus().equals(nextState + "_PENDING")) || (settlement.getStatus().equals(priorState))) {
                                    settlement.setStatus(nextState);
                                    settlement.setApplicationMessageId(Utils.applicationMessageIdToString(hcsResponse.getApplicationMessageId()));
                                    settlement.setAdditionalNotes(settleCompleteBPM.getAdditionalNotes());
                                    settlement.setPaymentReference(settleCompleteBPM.getPaymentReference());
                                    
                                    settlementRepository.save(settlement);
                                    // update the credits too
                                    updateCreditStateForSettlementItems(threadId, nextState);
                                    notify("settlements", settlement.getPayerName(), settlement.getRecipientName(),threadId);
                                } else {
                                    log.error("Settlement status should be " + nextState + "_PENDING" + " or " + priorState + ", found : " + settlement.getStatus());
                                }
                            } else if (settlement.getPaymentChannelName().equals(this.appData.getUserName())) {
                                if (settlement.getStatus().equals(States.SETTLE_PAY_ACK.name())) {
                                    settlement.setStatus(nextState);
                                    settlement.setApplicationMessageId(Utils.applicationMessageIdToString(hcsResponse.getApplicationMessageId()));
                                    settlement.setAdditionalNotes(settleCompleteBPM.getAdditionalNotes());
                                    settlement.setPaymentReference(settleCompleteBPM.getPaymentReference());
                                    
                                    settlementRepository.save(settlement);
                                    // update the credits too
                                    updateCreditStateForSettlementItems(threadId, nextState);
                                    notify("settlements", settlement.getPayerName(), settlement.getRecipientName(),threadId);

                                    try {
                                        hcsMessages.settlePaymentCompleteAck(this.appData, threadId, settlementBPM.getAutomatic(), settleCompleteBPM);
                                    } catch (Exception e) {
                                        log.error(e);
                                    }
                                    notify("settlements", settlement.getPayerName(), settlement.getRecipientName(), threadId);
                                } else {
                                    log.error("Settlement status should be " + nextState + "_PENDING" + " or " + priorState + ", found : " + settlement.getStatus());
                                }
                            }
                        }
                );
            } else if (settlementBPM.hasSettleCompleteAck()) {
                log.debug("settlementBPM.hasSettleCompleteAck()");
                settlementRepository.findById(threadId).ifPresent(
                    (settlement) -> {
                        if (settlement.getPayerName().equals(this.appData.getUserName())) {
                            updateSettlement(threadId, States.SETTLE_PAY_CONFIRMED, States.SETTLE_COMPLETE);
                        } else if (settlement.getPaymentChannelName().equals(this.appData.getUserName())) {
                            updateSettlement(threadId, States.SETTLE_PAY_CONFIRMED, States.SETTLE_COMPLETE);
                        }
                    }
                );
            } else if (settlementBPM.hasAdminDelete()) {
                log.debug("settlementBPM.hasAdminDelete()");
                deleteData();
                notify("admin", "admin", "admin", "admin");
            }  else if (settlementBPM.hasAdminStashDatabaseBPM()) {
                log.debug("settlementBPM.hasAdminStashDatabaseBPM()");
                stashData();
             
                notify("admin", "admin", "admin", "admin");
            } else if (settlementBPM.hasAdminStashPopDatabaseBPM()) {
                log.debug("settlementBPM.hasAdminStashPopDatabaseBPM()");
                stashPopData();
         
                notify("admin", "admin", "admin", "admin");
            } else {
                log.error ("Unrecognized application message");
            }
        } catch (Exception e) {
            log.error(e);
        }
    }
    
    private void notify(String context, String payer, String recipient, String threadId) {
        if ((this.stompSession == null) || ( ! this.stompSession.isConnected())) {
            WebSocketClient client = new StandardWebSocketClient();
            WebSocketStompClient stompClient = new WebSocketStompClient(client);        
            stompClient.setMessageConverter(new MappingJackson2MessageConverter());
            StompSessionHandler sessionHandler = new CustomStompSessionHandler(); 
            try {
                this.stompSession = stompClient.connect("ws://localhost:"+ this.appData.getWebPort() +"/notifications", sessionHandler).get();
            } catch (InterruptedException | ExecutionException e) {
                log.error(e);
            }
        }

        if (this.stompSession != null) {
            NotificationMessage notificationMessage = new NotificationMessage();
            notificationMessage.setContext(context);
            notificationMessage.setPayer(payer);
            notificationMessage.setRecipient(recipient);
            notificationMessage.setThreadId(threadId);
            
            this.stompSession.send("/hcsapp/notifications",notificationMessage);
        }

    }
    
    private void updateCreditStateForSettlementItems(String threadId, String newState) {
        settlementItemRepository.findAllSettlementItems(threadId).forEach(
                (settlementItem) -> {
                    creditRepository.findById(settlementItem.getId().getSettledThreadId()).ifPresent(
                            (credit) -> {
                                credit.setStatus(newState);
                                creditRepository.save(credit);
                            }
                    );
                }
        );
    }
    
    private void deleteData() {
        SxcPersistence persistence = this.appData.getHCSCore().getPersistence();

        persistence.clear();
        
        creditRepository.deleteAll();
        settlementRepository.deleteAll();
        settlementItemRepository.deleteAll();
    }
    
    private void stashData(){
        repositoryUtil.stashData();
    }
    
    private void stashPopData(){
        repositoryUtil.stashPopData();
    }
    
    private void updateSettlement(String threadId, States priorState, States newState) {
        // update the settlement state
        settlementRepository.findById(threadId).ifPresent(
                (settlement) -> {
                    if ((settlement.getStatus().equals(newState.name() + "_PENDING")) || (settlement.getStatus().equals(priorState.name()))) {
                        settlement.setStatus(newState.name());
                        settlementRepository.save(settlement);
                        // update the credits too
                        updateCreditStateForSettlementItems(threadId, newState.name());
                        notify("settlements", settlement.getPayerName(), settlement.getRecipientName(),threadId);
                    } else if (!settlement.getStatus().equals(newState.name())){
                        log.error("Settlement status should be " + newState.name() + "_PENDING" + " or " + priorState.name() + ", found : " + settlement.getStatus());
                    }
                }
        );
        
    }
    
    private void updateCredit(String threadId, States priorState, States newState) {
        // update the credit state
        creditRepository.findById(threadId).ifPresentOrElse(
                (credit) -> {
                    if ((credit.getStatus().equals(newState.name() + "_PENDING")) || (credit.getStatus().equals(priorState.name()))) {
                        credit.setStatus(newState.name());
                        creditRepository.save(credit);
                        notify("credits", credit.getPayerName(), credit.getRecipientName(),threadId);
                    } else {
                        log.error("Credit status should be " + newState.name() + "_PENDING" + " or " + priorState.name() + ", found : " + credit.getStatus());
                    }
                },
                () -> {
                    log.error("No credit found for threadId: " + threadId);
                }
        );
    }
}
