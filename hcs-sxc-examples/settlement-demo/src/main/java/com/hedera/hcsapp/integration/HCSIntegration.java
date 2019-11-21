package com.hedera.hcsapp.integration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.hedera.hcsapp.AppData;
import com.hedera.hcsapp.Enums;
import com.hedera.hcsapp.Utils;
import com.hedera.hcsapp.entities.Credit;
import com.hedera.hcsapp.entities.Settlement;
import com.hedera.hcsapp.entities.SettlementItem;
import com.hedera.hcsapp.entities.SettlementItemId;
import com.hedera.hcsapp.repository.CreditRepository;
import com.hedera.hcsapp.repository.SettlementItemRepository;
import com.hedera.hcsapp.repository.SettlementRepository;
import com.hedera.hcslib.callback.OnHCSMessageCallback;
import com.hedera.hcslib.consensus.HCSResponse;
import com.hedera.hcslib.proto.java.ApplicationMessage;
import com.hedera.hcslib.proto.java.TransactionID;

import lombok.extern.log4j.Log4j2;
import proto.CreditAckBPM;
import proto.CreditBPM;
import proto.SettleProposeAckBPM;
import proto.SettleProposeBPM;
import proto.SettlementBPM;

@Log4j2
@Component
public class HCSIntegration {

    private static AppData appData;

    @Autowired
    CreditRepository creditRepository;

    @Autowired
    SettlementRepository settlementRepository;

    @Autowired
    SettlementItemRepository settlementItemRepository;

    public HCSIntegration() throws Exception {
        appData = new AppData();
        // create a callback object to receive the message
        OnHCSMessageCallback onHCSMessageCallback = new OnHCSMessageCallback(appData.getHCSLib());
        onHCSMessageCallback.addObserver(hcsMessage -> {
            processHCSMessage(hcsMessage);
        });
    }

    public void processHCSMessage(HCSResponse hcsResponse) {
        try {
            ApplicationMessage applicationMessage = ApplicationMessage.parseFrom(hcsResponse.getMessage());

            SettlementBPM settlementBPM = SettlementBPM.parseFrom(applicationMessage.getBusinessProcessMessage().toByteArray());
            // (CREDIT_PENDING , r ,threadId ,credit) => (CREDIT_AWAIT_ACK ,r ,threadId , credit[threadId].txId=r.MessageId)
            if (settlementBPM.hasCredit()) {
                String priorState = Enums.state.CREDIT_PENDING.name();
                String nextState = Enums.state.CREDIT_AWAIT_ACK.name();

                CreditBPM creditBPM = settlementBPM.getCredit();
                String threadId = creditBPM.getThreadId();
                // update the credit state
                creditRepository.findById(threadId).ifPresentOrElse(
                        (credit) -> {
                            if (credit.getStatus().equals(priorState)) {
                                credit.setStatus(nextState);
                                credit.setTransactionId(Utils.TransactionIdToString(hcsResponse.getApplicationMessageId()));
                                creditRepository.save(credit);
                            } else {
                                log.error("Credit status should be " + priorState + ", found : " + credit.getStatus());
                            }
                        },
                        () -> {
                            Credit credit = Utils.creditFromCreditBPM(creditBPM);
                            credit.setStatus(nextState);
                            credit.setTransactionId(Utils.TransactionIdToString(hcsResponse.getApplicationMessageId()));
                            creditRepository.save(credit);
                            log.info("Adding new credit to Database: " + threadId);
                        }
                );
                
            // (CREDIT_AWAIT_ACK , r , threadId ,credit) => (CREDIT_ACK , r , threadId , credit[threadId].status=CREDIT_ACK)
            } else if (settlementBPM.hasCreditAck()) {
                String priorState = Enums.state.CREDIT_AWAIT_ACK.name();
                String nextState = Enums.state.CREDIT_ACK.name();

                CreditAckBPM creditAckBPM = settlementBPM.getCreditAck();
                String threadId = creditAckBPM.getThreadId();
                // update the credit state
                creditRepository.findById(threadId).ifPresentOrElse(
                        (credit) -> {
                            if (credit.getStatus().equals(priorState)) {
                                credit.setStatus(nextState);
                                creditRepository.save(credit);
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
                String priorState = Enums.state.SETTLE_PROPOSE_PENDING.name();
                String nextState = Enums.state.SETTLE_PROPOSE_AWAIT_ACK.name();

                SettleProposeBPM settleProposeBPM = settlementBPM.getSettlePropose();
                String threadId = settleProposeBPM.getThreadId();
                // update the settlement state
                settlementRepository.findById(threadId).ifPresentOrElse(
                        (settlement) -> {
                            if (settlement.getStatus().equals(priorState)) {
                                settlement.setStatus(nextState);
                                settlement.setTransactionId(Utils.TransactionIdToString(hcsResponse.getApplicationMessageId()));
                                settlementRepository.save(settlement);
                            } else {
                                log.error("Settlement status should be " + priorState + ", found : " + settlement.getStatus());
                            }
                        },
                        () -> {
                            Settlement settlement = Utils.settlementFromSettleProposeBPM(settleProposeBPM);
                            settlement.setStatus(nextState);
                            settlement.setTransactionId(Utils.TransactionIdToString(hcsResponse.getApplicationMessageId()));
                            settlementRepository.save(settlement);
                            log.info("Adding new settlement to Database: " + threadId);

                            for (String settleThreadId : settleProposeBPM.getThreadIdsList()) {
                                SettlementItem settlementItem = new SettlementItem();
                                settlementItem.setId(new SettlementItemId(settleThreadId, threadId));
                                settlementItemRepository.save(settlementItem);
                            }
                        }
                );
            } else if (settlementBPM.hasSettleProposeAck()) {
                String priorState = Enums.state.SETTLE_PROPOSE_AWAIT_ACK.name();
                String nextState = Enums.state.SETTLE_PROPOSE_ACK.name();

                SettleProposeAckBPM settleProposeAckBPM = settlementBPM.getSettleProposeAck();
                String threadId = settleProposeAckBPM.getThreadId();
                // update the settlement state
                settlementRepository.findById(threadId).ifPresentOrElse(
                        (settlement) -> {
                            if (settlement.getStatus().equals(priorState)) {
                                settlement.setStatus(nextState);
                                settlementRepository.save(settlement);
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
}
