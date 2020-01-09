package com.hedera.hcsapp.integration;

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
import com.hedera.hcs.sxc.interfaces.HCSResponse;
import com.hedera.hcs.sxc.interfaces.SxcMessagePersistence;
import com.hedera.hcsapp.AppData;
import com.hedera.hcsapp.States;
import com.hedera.hcsapp.Utils;
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
import com.hedera.hcs.sxc.proto.java.ApplicationMessage;

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
    private Util repositoryUtil;
            
    @Autowired
    private CreditRepository creditRepository;

    @Autowired
    private SettlementRepository settlementRepository;

    @Autowired
    private SettlementItemRepository settlementItemRepository;
    
    private StompSession stompSession;

    public HCSIntegration() throws Exception {
        this.appData = new AppData();
        // create a callback object to receive the message
        OnHCSMessageCallback onHCSMessageCallback = new OnHCSMessageCallback(this.appData.getHCSCore());
        onHCSMessageCallback.addObserver(hcsMessage -> {
            processHCSMessage(hcsMessage);
        });
    }

    public void processHCSMessage(HCSResponse hcsResponse) {
        try {
            ApplicationMessage applicationMessage = ApplicationMessage.parseFrom(hcsResponse.getMessage());

            SettlementBPM settlementBPM = SettlementBPM.parseFrom(applicationMessage.getBusinessProcessMessage().toByteArray());
            // (CREDIT_PENDING , r ,threadId ,credit) => (CREDIT_AWAIT_ACK ,r ,threadId , credit[threadId].txId=r.MessageId)
            String threadId = settlementBPM.getThreadId();
            if (settlementBPM.hasCredit()) {
                String priorState = States.CREDIT_PROPOSED_PENDING.name();
                String nextState = States.CREDIT_PROPOSED.name();

                CreditBPM creditBPM = settlementBPM.getCredit();
                // update the credit state
                creditRepository.findById(threadId).ifPresentOrElse(
                        (credit) -> {
                            if (credit.getStatus().equals(priorState)) {
                                credit.setStatus(nextState);
                                credit.setApplicationMessageId(Utils.TransactionIdToString(hcsResponse.getApplicationMessageId()));
                                creditRepository.save(credit);
                                notify("credits", credit.getPayerName(), credit.getRecipientName(),threadId);
                            } else {
                                log.error("Credit status should be " + priorState + ", found : " + credit.getStatus());
                            }
                        },
                        () -> {
                            Credit credit = Utils.creditFromCreditBPM(creditBPM, threadId);
                            credit.setStatus(nextState);
                            credit.setApplicationMessageId(Utils.TransactionIdToString(hcsResponse.getApplicationMessageId()));
                            creditRepository.save(credit);
                            notify("credits", credit.getPayerName(), credit.getRecipientName(),threadId);
                            log.info("Adding new credit to Database: " + threadId);
                        }
                );
                
            // (CREDIT_AWAIT_ACK , r , threadId ,credit) => (CREDIT_ACK , r , threadId , credit[threadId].status=CREDIT_ACK)
            } else if (settlementBPM.hasCreditAck()) {
                updateCredit(threadId, States.CREDIT_PROPOSED, States.CREDIT_AGREED);
            } else if (settlementBPM.hasSettlePropose()) {
                String nextState = States.SETTLEMENT_PROPOSED.name();

                SettleProposeBPM settleProposeBPM = settlementBPM.getSettlePropose();
                // update the settlement state
                settlementRepository.findById(threadId).ifPresentOrElse(
                        (settlement) -> {
                            if (settlement.getStatus().equals(nextState + "_PENDING")) {
                                settlement.setStatus(nextState);
                                settlement.setApplicationMessageId(Utils.TransactionIdToString(hcsResponse.getApplicationMessageId()));
                                settlementRepository.save(settlement);
                                // update the credits too
                                updateCreditStateForSettlementItems(threadId, nextState);
                                notify("settlements", settlement.getPayerName(), settlement.getRecipientName(),threadId);
                            } else {
                                log.error("Settlement status should be " + nextState + "_PENDING" + ", found : " + settlement.getStatus());
                            }
                        },
                        () -> {
                            Settlement settlement = Utils.settlementFromSettleProposeBPM(settleProposeBPM, threadId);
                            settlement.setStatus(nextState);
                            settlement.setApplicationMessageId(Utils.TransactionIdToString(hcsResponse.getApplicationMessageId()));
                            settlementRepository.save(settlement);
                            log.info("Adding new settlement to Database: " + threadId);

                            for (String settleThreadId : settleProposeBPM.getThreadIdsList()) {
                                SettlementItem settlementItem = new SettlementItem();
                                settlementItem.setId(new SettlementItemId(settleThreadId, threadId));
                                settlementItemRepository.save(settlementItem);
                            }
                            // update the credits too
                            updateCreditStateForSettlementItems(threadId, nextState);

                            notify("settlements", settlement.getPayerName(), settlement.getRecipientName(),threadId);
                        }
                );
            } else if (settlementBPM.hasSettleProposeAck()) {
                updateSettlement(threadId, States.SETTLEMENT_PROPOSED, States.SETTLEMENT_AGREED);
            } else if (settlementBPM.hasSettleInit()) {
                String priorState = States.SETTLEMENT_AGREED.name();
                String nextState = States.SETTLE_INIT_AWAIT_ACK.name();

                SettleInitBPM settleInitBPM = settlementBPM.getSettleInit();
                // update the settlement state
                settlementRepository.findById(threadId).ifPresent(
                        (settlement) -> {
                            if ((settlement.getStatus().equals(nextState + "_PENDING")) || (settlement.getStatus().equals(priorState))) {
                                settlement.setStatus(nextState);
                                settlement.setApplicationMessageId(Utils.TransactionIdToString(hcsResponse.getApplicationMessageId()));
                                settlement.setAdditionalNotes(settleInitBPM.getAdditionalNotes());
                                settlement.setPaymentChannelName(settleInitBPM.getPaymentChannelName());
                                settlementRepository.save(settlement);
                                // update the credits too
                                updateCreditStateForSettlementItems(threadId, nextState);
                                notify("settlements", settlement.getPayerName(), settlement.getRecipientName(),threadId);
                            } else {
                                log.error("Settlement status should be " + priorState + " or " + nextState + "_PENDING" + ", found : " + settlement.getStatus());
                            }
                        }
                );
            } else if (settlementBPM.hasSettleInitAck()) {
                updateSettlement(threadId, States.SETTLE_INIT_AWAIT_ACK, States.SETTLE_INIT_ACK);
            } else if (settlementBPM.hasPaymentInit()) {
                
                String priorState = States.SETTLE_INIT_ACK.name();
                String nextState = States.PAYMENT_INIT_AWAIT_ACK.name();

                PaymentInitBPM paymentInitBPM = settlementBPM.getPaymentInit();
                // update the settlement state
                settlementRepository.findById(threadId).ifPresent(
                        (settlement) -> {
                            if ((settlement.getStatus().equals(nextState + "_PENDING")) || (settlement.getStatus().equals(priorState))) {
                                settlement.setStatus(nextState);
                                settlement.setApplicationMessageId(Utils.TransactionIdToString(hcsResponse.getApplicationMessageId()));
                                settlement.setAdditionalNotes(paymentInitBPM.getAdditionalNotes());
                                settlement.setPayerAccountDetails(paymentInitBPM.getPayerAccountDetails());
                                settlement.setRecipientAccountDetails(paymentInitBPM.getRecipientAccountDetails());
                                
                                settlementRepository.save(settlement);
                                // update the credits too
                                updateCreditStateForSettlementItems(threadId, nextState);
                                notify("settlements", settlement.getPayerName(), settlement.getRecipientName(),threadId);
                            } else {
                                log.error("Settlement status should be " + priorState + " or " + nextState + ", found : " + settlement.getStatus());
                            }
                        }
                );
            } else if (settlementBPM.hasPaymentInitAck()) {
                updateSettlement(threadId, States.PAYMENT_INIT_AWAIT_ACK, States.PAYMENT_INIT_ACK);
            } else if (settlementBPM.hasPaymentSent()) {
                String priorState = States.PAYMENT_INIT_ACK.name();
                String nextState = States.PAYMENT_SENT_AWAIT_ACK.name();

                PaymentSentBPM paymentSentBPM = settlementBPM.getPaymentSent();
                // update the settlement state
                settlementRepository.findById(threadId).ifPresent(
                        (settlement) -> {
                            if ((settlement.getStatus().equals(nextState + "_PENDING")) || (settlement.getStatus().equals(priorState))) {
                                settlement.setStatus(nextState);
                                settlement.setApplicationMessageId(Utils.TransactionIdToString(hcsResponse.getApplicationMessageId()));
                                settlement.setAdditionalNotes(paymentSentBPM.getAdditionalNotes());
                                settlement.setPaymentReference(paymentSentBPM.getPaymentReference());
                                
                                settlementRepository.save(settlement);
                                // update the credits too
                                updateCreditStateForSettlementItems(threadId, nextState);
                                notify("settlements", settlement.getPayerName(), settlement.getRecipientName(),threadId);
                            } else {
                                log.error("Settlement status should be " + nextState + "_PENDING" + " or " + priorState + ", found : " + settlement.getStatus());
                            }
                        }
                );

            } else if (settlementBPM.hasPaymentSentAck()) {
                updateSettlement(threadId, States.PAYMENT_SENT_AWAIT_ACK, States.PAYMENT_SENT_ACK);
            } else if (settlementBPM.hasSettlePayment()) {
                String priorState = States.PAYMENT_SENT_ACK.name();
                String nextState = States.SETTLE_PAID_AWAIT_ACK.name();

                SettlePaidBPM settlePaidBPM = settlementBPM.getSettlePayment();
                // update the settlement state
                settlementRepository.findById(threadId).ifPresent(
                        (settlement) -> {
                            if ((settlement.getStatus().equals(nextState + "_PENDING")) || (settlement.getStatus().equals(priorState))) {
                                settlement.setStatus(nextState);
                                settlement.setApplicationMessageId(Utils.TransactionIdToString(hcsResponse.getApplicationMessageId()));
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
                );
            } else if (settlementBPM.hasSettlePaymentAck()) {
                updateSettlement(threadId, States.SETTLE_PAID_AWAIT_ACK, States.SETTLE_PAID_ACK);
            } else if (settlementBPM.hasSettleComplete()) {
                String priorState = States.SETTLE_PAID_ACK.name();
                String nextState = States.SETTLE_COMP_AWAIT_ACK.name();

                SettleCompleteBPM settleCompleteBPM = settlementBPM.getSettleComplete();
                // update the settlement state
                settlementRepository.findById(threadId).ifPresent(
                        (settlement) -> {
                            if ((settlement.getStatus().equals(nextState + "_PENDING")) || (settlement.getStatus().equals(priorState))) {
                                settlement.setStatus(nextState);
                                settlement.setApplicationMessageId(Utils.TransactionIdToString(hcsResponse.getApplicationMessageId()));
                                settlement.setAdditionalNotes(settleCompleteBPM.getAdditionalNotes());
                                settlement.setPaymentReference(settleCompleteBPM.getPaymentReference());
                                
                                settlementRepository.save(settlement);
                                // update the credits too
                                updateCreditStateForSettlementItems(threadId, nextState);
                                notify("settlements", settlement.getPayerName(), settlement.getRecipientName(),threadId);
                            } else {
                                log.error("Settlement status should be " + nextState + "_PENDING" + " or " + priorState + ", found : " + settlement.getStatus());
                            }
                        }
                );
            } else if (settlementBPM.hasSettleCompleteAck()) {
                updateSettlement(threadId, States.SETTLE_COMP_AWAIT_ACK, States.SETTLE_COMPLETE_ACK);
            } else if (settlementBPM.hasAdminDelete()) {
                deleteData();
                notify("admin", "admin", "admin", "admin");
            }  else if (settlementBPM.hasAdminStashDatabaseBPM()) {
                stashData();
             
                notify("admin", "admin", "admin", "admin");
            } else if (settlementBPM.hasAdminStashPopDatabaseBPM()) {
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
                this.stompSession = stompClient.connect("ws://localhost:8080/notifications", sessionHandler).get();
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
        SxcMessagePersistence persistence = this.appData.getHCSCore().getMessagePersistence();

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
                    } else {
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
