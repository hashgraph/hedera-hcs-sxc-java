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

import com.hedera.hcsapp.AppData;
import com.hedera.hcsapp.Enums;
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
import com.hedera.hcslib.callback.OnHCSMessageCallback;
import com.hedera.hcslib.consensus.HCSResponse;
import com.hedera.hcslib.proto.java.ApplicationMessage;
import lombok.extern.log4j.Log4j2;
import proto.CreditBPM;
import proto.SettleProposeBPM;
import proto.SettlementBPM;

@Log4j2
@Component
public class HCSIntegration {

    private AppData appData;
    
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
        OnHCSMessageCallback onHCSMessageCallback = new OnHCSMessageCallback(appData.getHCSLib());
        onHCSMessageCallback.addObserver(hcsMessage -> {
            processHCSMessage(hcsMessage);
        });
    }

    public void processHCSMessage(HCSResponse hcsResponse) {
        if (this.stompSession == null) {
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

        try {
            ApplicationMessage applicationMessage = ApplicationMessage.parseFrom(hcsResponse.getMessage());

            SettlementBPM settlementBPM = SettlementBPM.parseFrom(applicationMessage.getBusinessProcessMessage().toByteArray());
            // (CREDIT_PENDING , r ,threadId ,credit) => (CREDIT_AWAIT_ACK ,r ,threadId , credit[threadId].txId=r.MessageId)
            String threadId = settlementBPM.getThreadId();
            if (settlementBPM.hasCredit()) {
                String priorState = Enums.state.CREDIT_PROPOSED_PENDING.name();
                String nextState = Enums.state.CREDIT_PROPOSED.name();

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
                String priorState = Enums.state.CREDIT_PROPOSED.name();
                String nextState = Enums.state.CREDIT_AGREED.name();

                // update the credit state
                creditRepository.findById(threadId).ifPresentOrElse(
                        (credit) -> {
                            if (credit.getStatus().equals(priorState)) {
                                credit.setStatus(nextState);
                                creditRepository.save(credit);
                                notify("credits", credit.getPayerName(), credit.getRecipientName(),threadId);
                            } else {
                                log.error("Credit status should be " + priorState + ", found : " + credit.getStatus());
                            }
                        },
                        () -> {
                            log.error("No credit found for threadId: " + threadId);
                        }
                );
            } else if (settlementBPM.hasPaymentInit()) {

            } else if (settlementBPM.hasPaymentInitAck()) {

            } else if (settlementBPM.hasPaymentSent()) {

            } else if (settlementBPM.hasPaymentSentAck()) {

            } else if (settlementBPM.hasSettleComplete()) {

            } else if (settlementBPM.hasSettleCompleteAck()) {

            } else if (settlementBPM.hasSettleInit()) {

            } else if (settlementBPM.hasSettleInitAck()) {

            } else if (settlementBPM.hasSettlePayment()) {

            } else if (settlementBPM.hasSettlePaymentAck()) {

            } else if (settlementBPM.hasSettlePropose()) {
                String priorState = Enums.state.SETTLEMENT_PROPOSED_PENDING.name();
                String nextState = Enums.state.SETTLEMENT_PROPOSED.name();

                SettleProposeBPM settleProposeBPM = settlementBPM.getSettlePropose();
                // update the settlement state
                settlementRepository.findById(threadId).ifPresentOrElse(
                        (settlement) -> {
                            if (settlement.getStatus().equals(priorState)) {
                                settlement.setStatus(nextState);
                                settlement.setApplicationMessageId(Utils.TransactionIdToString(hcsResponse.getApplicationMessageId()));
                                settlementRepository.save(settlement);
                                // update the credits too
                                updateCreditStateForSettlementItems(threadId, nextState);
                                notify("settlements", settlement.getPayerName(), settlement.getRecipientName(),threadId);
                            } else {
                                log.error("Settlement status should be " + priorState + ", found : " + settlement.getStatus());
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
                String priorState = Enums.state.SETTLEMENT_PROPOSED.name();
                String nextState = Enums.state.SETTLEMENT_AGREED.name();

                // update the settlement state
                settlementRepository.findById(threadId).ifPresentOrElse(
                        (settlement) -> {
                            if (settlement.getStatus().equals(priorState)) {
                                settlement.setStatus(nextState);
                                settlementRepository.save(settlement);

                                // update the credits too
                                updateCreditStateForSettlementItems(threadId, nextState);

                                notify("settlements", settlement.getPayerName(), settlement.getRecipientName(),threadId);
                            } else {
                                log.error("Settlement status should be " + priorState + ", found : " + settlement.getStatus());
                            }
                        },
                        () -> {
                            log.error("No settlement found for threadId: " + threadId);
                        }
                );
            } else {
                log.error ("Unrecognized application message");
            }
        } catch (Exception e) {
            log.error(e);
        }
    }
    
    private void notify(String context, String payer, String recipient, String threadId) {
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
}
