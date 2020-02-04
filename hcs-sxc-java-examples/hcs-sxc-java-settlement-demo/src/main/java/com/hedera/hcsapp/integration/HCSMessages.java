package com.hedera.hcsapp.integration;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import org.springframework.http.HttpHeaders;
import com.hedera.hashgraph.sdk.TransactionId;
import com.hedera.hcs.sxc.consensus.OutboundHCSMessage;
import com.hedera.hcsapp.AppData;
import com.hedera.hcsapp.States;
import com.hedera.hcsapp.Utils;
import com.hedera.hcsapp.entities.Credit;
import com.hedera.hcsapp.entities.Settlement;
import com.hedera.hcsapp.entities.SettlementItem;
import com.hedera.hcsapp.entities.SettlementItemId;
import com.hedera.hcsapp.repository.CreditRepository;
import com.hedera.hcsapp.repository.SettlementItemRepository;
import com.hedera.hcsapp.repository.SettlementRepository;
import com.hedera.hcsapp.restclasses.CreditProposal;
import com.hedera.hcsapp.restclasses.CreditRest;
import com.hedera.hcsapp.restclasses.SettlementProposal;
import com.hedera.hcsapp.restclasses.SettlementRest;

import lombok.extern.log4j.Log4j2;
import proto.CreditAckBPM;
import proto.CreditBPM;
import proto.Money;
import proto.PaymentInitAckBPM;
import proto.PaymentInitBPM;
import proto.PaymentSentAckBPM;
import proto.PaymentSentBPM;
import proto.SettleCompleteAckBPM;
import proto.SettleCompleteBPM;
import proto.SettleInitAckBPM;
import proto.SettleInitBPM;
import proto.SettlePaidAckBPM;
import proto.SettlePaidBPM;
import proto.SettleProposeAckBPM;
import proto.SettleProposeBPM;
import proto.SettlementBPM;

@Log4j2
public final class HCSMessages {
    
    public static CreditRest creditNew(AppData appData, CreditRepository creditRepository, CreditProposal creditCreate) throws Exception {
        log.info("creditNew");
        Instant now = Instant.now();
        Long seconds = now.getEpochSecond();
        int nanos = now.getNano();
        String threadId = Utils.getThreadId();

        Money value = Money.newBuilder()
                .setCurrencyCode(creditCreate.getCurrency())
                .setUnits(creditCreate.getAmount())
                .build();
        CreditBPM creditBPM = CreditBPM.newBuilder()
                .setAdditionalNotes(creditCreate.getAdditionalNotes())
                .setPayerName(creditCreate.getPayerName())
                .setRecipientName(creditCreate.getRecipientName())
                .setServiceRef(creditCreate.getReference())
                .setValue(value)
                .setCreatedDate(Utils.TimestampToDate(seconds, nanos))
                .setCreatedTime(Utils.TimestampToTime(seconds, nanos))
                .build();
        SettlementBPM settlementBPM = SettlementBPM.newBuilder()
                .setThreadID(threadId)
                .setCredit(creditBPM)
                .setAutomatic(creditCreate.isAutomatic())
                .build();

        TransactionId transactionId = new TransactionId(appData.getHCSCore().getOperatorAccountId());

        Credit credit = new Credit();
        // copy data from new credit
        credit.setAdditionalNotes(creditCreate.getAdditionalNotes());
        credit.setAmount(creditCreate.getAmount());
        credit.setCurrency(creditCreate.getCurrency());
        credit.setPayerName(creditCreate.getPayerName());
        credit.setRecipientName(creditCreate.getRecipientName());
        credit.setReference(creditCreate.getReference());

        credit.setCreatedDate(Utils.TimestampToDate(seconds, nanos));
        credit.setCreatedTime(Utils.TimestampToTime(seconds, nanos));
        credit.setApplicationMessageId(Utils.TransactionIdToString(transactionId));
        credit.setAutomatic(creditCreate.isAutomatic());
        credit.setThreadId(threadId);
        
        credit = creditRepository.save(credit);
        
        credit = creditRepository.findById(threadId).get();
        if ((credit.getStatus() == null) || ( ! credit.getStatus().contentEquals(States.CREDIT_PROPOSED.name()))) {
            // avoiding race condition
            credit.setStatus(States.CREDIT_PROPOSED_PENDING.name());
            credit = creditRepository.save(credit);
        } else {
            log.error("Credit state is already CREDIT_PROPOSED");
        }
        
        credit = creditRepository.save(credit);
        new OutboundHCSMessage(appData.getHCSCore())
              .overrideEncryptedMessages(false)
              .overrideMessageSignature(false)
              .withFirstTransactionId(transactionId)
              .sendMessage(appData.getTopicIndex(), settlementBPM.toByteArray());

        log.info("Message sent successfully.");

        return new CreditRest(credit, appData);
    }
    public static CreditRest creditAck(AppData appData, CreditRepository creditRepository, String threadId, boolean automatic) throws Exception {
        log.info("creditAck");
        Credit credit = creditRepository.findById(threadId).get();

        CreditBPM creditBPM = Utils.creditBPMFromCredit(credit);

        CreditAckBPM creditAckBPM = CreditAckBPM.newBuilder()
                .setCredit(creditBPM)
                .build();

        SettlementBPM settlementBPM = SettlementBPM.newBuilder()
                .setThreadID(threadId)
                .setCreditAck(creditAckBPM)
                .setAutomatic(automatic)
                .build();

        if ( ! credit.getStatus().contentEquals(States.CREDIT_AGREED.name())) {
            // avoiding race condition
            credit.setStatus(States.CREDIT_AGREED_PENDING.name());
            credit = creditRepository.save(credit);
        } else {
            log.error("Credit state is already CREDIT_AGREED");
        }

        new OutboundHCSMessage(appData.getHCSCore())
              .overrideEncryptedMessages(false)
              .overrideMessageSignature(false)
              .sendMessage(appData.getTopicIndex(), settlementBPM.toByteArray());

        log.info("Message sent successfully.");

        return new CreditRest(credit, appData);
        
    }
    
    public static SettlementRest settlementNew(AppData appData, CreditRepository creditRepository, SettlementRepository settlementRepository, SettlementItemRepository settlementItemRepository, SettlementProposal settleProposal) throws Exception {
        log.info("settlementNew");
        Instant now = Instant.now();
        Long seconds = now.getEpochSecond();
        int nanos = now.getNano();

        String threadId = Utils.getThreadId();

        Money value = Money.newBuilder().setCurrencyCode(settleProposal.getCurrency())
                .setUnits(settleProposal.getNetValue()).build();
        SettleProposeBPM.Builder settleProposeBPM = SettleProposeBPM.newBuilder()
                .setAdditionalNotes(settleProposal.getAdditionalNotes()).setPayerName(settleProposal.getPayerName())
                .setRecipientName(settleProposal.getRecipientName())
                .setCreatedDate(Utils.TimestampToDate(seconds, nanos))
                .setCreatedTime(Utils.TimestampToTime(seconds, nanos)).setNetValue(value);

        for (String proposedThreadId : settleProposal.getThreadIds()) {
            settleProposeBPM.addThreadIDs(proposedThreadId);
        }

        SettlementBPM settlementBPM = SettlementBPM.newBuilder().setThreadID(threadId)
                .setSettlePropose(settleProposeBPM.build())
                .setAutomatic(settleProposal.isAutomatic())
                .build();

        TransactionId transactionId = new TransactionId(appData.getHCSCore().getOperatorAccountId());

        Settlement settlement = new Settlement();
        // copy data
        settlement.setAdditionalNotes(settleProposal.getAdditionalNotes());
        settlement.setCurrency(settleProposal.getCurrency());
        settlement.setNetValue(settleProposal.getNetValue());
        settlement.setPayerName(settleProposal.getPayerName());
        settlement.setRecipientName(settleProposal.getRecipientName());
        settlement.setThreadId(threadId);
        settlement.setApplicationMessageId(Utils.TransactionIdToString(transactionId));
        settlement.setCreatedDate(Utils.TimestampToDate(seconds, nanos));
        settlement.setCreatedTime(Utils.TimestampToTime(seconds, nanos));

        settlement = settlementRepository.save(settlement);

        settlement = settlementRepository.findById(threadId).get();
        if ((settlement.getStatus() == null)
                || (!settlement.getStatus().contentEquals(States.SETTLE_PROPOSED.name()))) {
            settlement.setStatus(States.SETTLE_PROPOSED_PENDING.name());
            settlement = settlementRepository.save(settlement);
        } else {
            log.error("Settlement state is already SETTLEMENT_PROPOSED");
        }

        // now settlement items
        for (String settledThreadId : settleProposal.getThreadIds()) {
            SettlementItem settlementItem = new SettlementItem();
            settlementItem.setId(new SettlementItemId(settledThreadId, threadId));
            settlementItem = settlementItemRepository.save(settlementItem);
        }

        new OutboundHCSMessage(appData.getHCSCore()).overrideEncryptedMessages(false).overrideMessageSignature(false)
                .withFirstTransactionId(transactionId).sendMessage(appData.getTopicIndex(), settlementBPM.toByteArray());

        log.info("Message sent successfully.");

        return new SettlementRest(settlement, appData, settlementItemRepository,
                creditRepository);
    }
    
    public static SettlementRest settlementAck(AppData appData, SettlementRepository settlementRepository, SettlementItemRepository settlementItemRepository, CreditRepository creditRepository, String threadId, boolean automatic) throws Exception {
        log.info("settlementAck");
        Optional<Settlement> settlement = settlementRepository.findById(threadId);

        if (settlement.isPresent()) {
            if (settlement.get().getStatus().contentEquals(States.SETTLE_PROPOSED.name())) {
                SettleProposeBPM.Builder settleProposeBPM = SettleProposeBPM.newBuilder()
                        .setAdditionalNotes(settlement.get().getAdditionalNotes()).setNetValue(Utils.moneyFromSettlement(settlement.get()))
                        .setPayerName(settlement.get().getPayerName())
                        .setRecipientName(settlement.get().getRecipientName());
    
                List<SettlementItem> settlementItems = settlementItemRepository.findAllSettlementItems(threadId);
                for (SettlementItem settlementItem : settlementItems) {
                    settleProposeBPM.addThreadIDs(settlementItem.getId().getSettledThreadId());
                }
    
                SettleProposeAckBPM settleProposeAck = SettleProposeAckBPM.newBuilder()
                        .setSettlePropose(settleProposeBPM.build()).build();
    
                SettlementBPM settlementBPM = SettlementBPM.newBuilder().setThreadID(threadId)
                        .setSettleProposeAck(settleProposeAck)
                        .setAutomatic(automatic)
                        .build();
    
                return saveAndSendSettlement(appData, settlementRepository, settlementItemRepository, creditRepository, settlementBPM, settlement.get(), States.SETTLE_AGREED);
            } else {
                String error = "Status is not " + States.SETTLE_PROPOSED.name() + " but " + settlement.get().getStatus() + ", not performing update";
                throw new Exception(error);
            }
        } else {
            String error = "Settlement not found for threadId " + threadId;
            throw new Exception(error);
        }
    }
    
    public static SettlementRest settlementInit(AppData appData, SettlementRepository settlementRepository, SettlementItemRepository settlementItemRepository, CreditRepository creditRepository, String threadId, boolean automatic, String additionalNotes, String paymentChannelName) throws Exception {
        log.info("settlementInit");
        Optional<Settlement> settlement = settlementRepository.findById(threadId);

        if (settlement.isPresent()) {

            SettleInitBPM.Builder settleInitBPM = SettleInitBPM.newBuilder()
                .setAdditionalNotes(additionalNotes)
                .setNetValue(Utils.moneyFromSettlement(settlement.get()))
                .setPayerName(settlement.get().getPayerName())
                .setRecipientName(settlement.get().getRecipientName())
                .setPaymentChannelName(paymentChannelName);

            SettlementBPM settlementBPM = SettlementBPM.newBuilder().setThreadID(threadId)
                    .setSettleInit(settleInitBPM)
                    .setAutomatic(automatic)
                    .build();
            return saveAndSendSettlement(appData, settlementRepository, settlementItemRepository, creditRepository, settlementBPM, settlement.get(), States.SETTLE_PAY_CHANNEL_PROPOSED);
        } else {
            String error = "Settlement not found for threadId " + threadId;
            throw new Exception(error);
        }
    }
    
    public static SettlementRest settleProposeChannelAck(AppData appData, SettlementRepository settlementRepository, SettlementItemRepository settlementItemRepository, CreditRepository creditRepository, String threadId, boolean automatic) throws Exception {
        log.info("settleProposeChannelAck");
        Optional<Settlement> settlement = settlementRepository.findById(threadId);

        if (settlement.isPresent()) {
            if (settlement.get().getStatus().contentEquals(States.SETTLE_PAY_CHANNEL_PROPOSED.name())) {
                SettleInitBPM.Builder settleInitBPM = SettleInitBPM.newBuilder()
                        .setAdditionalNotes(settlement.get().getAdditionalNotes())
                        .setNetValue(Utils.moneyFromSettlement(settlement.get()))
                        .setPayerName(settlement.get().getPayerName())
                        .setRecipientName(settlement.get().getRecipientName())
                        .setPaymentChannelName(settlement.get().getPaymentChannelName());
    
                SettleInitAckBPM.Builder settleInitAckBPM = SettleInitAckBPM.newBuilder().setSettleInit(settleInitBPM);
    
                SettlementBPM settlementBPM = SettlementBPM.newBuilder().setThreadID(threadId)
                        .setSettleInitAck(settleInitAckBPM)
                        .setAutomatic(automatic)
                        .build();
    
                return saveAndSendSettlement(appData, settlementRepository, settlementItemRepository, creditRepository, settlementBPM, settlement.get(), States.SETTLE_PAY_CHANNEL_AGREED);
            } else {
                String error = "Status is not " + States.SETTLE_PAY_CHANNEL_PROPOSED.name() + " but " + settlement.get().getStatus() + ", not performing update";
                throw new Exception(error);
            }
        } else {
            String error = "Settlement not found for threadId " + threadId;
            throw new Exception(error);
        }
    }
    
    public static SettlementRest settlePaymentInit (AppData appData, SettlementRepository settlementRepository, SettlementItemRepository settlementItemRepository, CreditRepository creditRepository, String threadId, boolean automatic, String payerAccountDetails, String recipientAccountDetails, String additionalNotes) throws Exception {
        log.info("settlePaymentInit");
        Optional<Settlement> settlement = settlementRepository.findById(threadId);
    
        if (settlement.isPresent()) {
            if (settlement.get().getStatus().contentEquals(States.SETTLE_PAY_CHANNEL_AGREED.name())) {
    
                PaymentInitBPM.Builder paymentInitBPM = PaymentInitBPM.newBuilder()
                        .setPayerName(settlement.get().getPayerName())
                        .setRecipientName(settlement.get().getRecipientName())
                        .setPayerAccountDetails(payerAccountDetails)
                        .setRecipientAccountDetails(recipientAccountDetails)
                        .setAdditionalNotes(additionalNotes)
                        .setNetValue(Utils.moneyFromSettlement(settlement.get()));
    
                SettlementBPM settlementBPM = SettlementBPM.newBuilder().setThreadID(threadId)
                        .setPaymentInit(paymentInitBPM)
                        .setAutomatic(automatic)
                        .build();
    
                return saveAndSendSettlement(appData, settlementRepository, settlementItemRepository, creditRepository, settlementBPM, settlement.get(), States.SETTLE_PAY_PROPOSED);
            } else {
                String error = "Status is not " + States.SETTLE_PAY_CHANNEL_AGREED.name() + " but " + settlement.get().getStatus() + ", not performing update";
                throw new Exception(error);
            }
        } else {
            String error = "Settlement not found for threadId " + threadId;
            throw new Exception(error);
        }
    }
    
    public static SettlementRest settlePaymentInitAck(AppData appData, SettlementRepository settlementRepository, SettlementItemRepository settlementItemRepository, CreditRepository creditRepository, String threadId, boolean automatic) throws Exception {
        log.info("settlePaymentInitAck");
        
        Optional<Settlement> settlement = settlementRepository.findById(threadId);

        if (settlement.isPresent()) {
            if (settlement.get().getStatus().contentEquals(States.SETTLE_PAY_PROPOSED.name())) {
                PaymentInitBPM.Builder paymentInitBPM = PaymentInitBPM.newBuilder()
                        .setPayerName(settlement.get().getPayerName())
                        .setRecipientName(settlement.get().getRecipientName())
                        .setPayerAccountDetails(settlement.get().getPayerAccountDetails())
                        .setRecipientAccountDetails(settlement.get().getRecipientAccountDetails())
                        .setAdditionalNotes(settlement.get().getAdditionalNotes())
                        .setNetValue(Utils.moneyFromSettlement(settlement.get()));
    
                PaymentInitAckBPM.Builder paymentInitAckBPM = PaymentInitAckBPM.newBuilder().setPaymentInit(paymentInitBPM);
    
                SettlementBPM settlementBPM = SettlementBPM.newBuilder().setThreadID(threadId)
                        .setPaymentInitAck(paymentInitAckBPM)
                        .setAutomatic(automatic)
                        .build();
    
                return saveAndSendSettlement(appData, settlementRepository, settlementItemRepository, creditRepository, settlementBPM, settlement.get(), States.SETTLE_PAY_AGREED);
            } else {
                String error = "Status is not " + States.SETTLE_PAY_PROPOSED.name() + " but " + settlement.get().getStatus() + ", not performing update";
                throw new Exception(error);
            }
        } else {
            String error = "Settlement not found for threadId " + threadId;
            throw new Exception(error);
        }
    }
    
    public static SettlementRest settlePaymentSent(AppData appData, SettlementRepository settlementRepository, SettlementItemRepository settlementItemRepository, CreditRepository creditRepository, String threadId, boolean automatic, String payref) throws Exception {
        log.info("settlePaymentSent");
        Optional<Settlement> settlement = settlementRepository.findById(threadId);

        if (settlement.isPresent()) {
            if (settlement.get().getStatus().contentEquals(States.SETTLE_PAY_AGREED.name())) {
                PaymentSentBPM.Builder paymentSentBPM = PaymentSentBPM.newBuilder()
                        .setPayerName(settlement.get().getPayerName())
                        .setRecipientName(settlement.get().getRecipientName())
                        .setPayerAccountDetails(settlement.get().getPayerAccountDetails())
                        .setRecipientAccountDetails(settlement.get().getRecipientAccountDetails())
                        .setAdditionalNotes("Bank Transfer Complete")
                        .setPaymentReference(payref)
                        .setNetValue(Utils.moneyFromSettlement(settlement.get()));

                SettlementBPM settlementBPM = SettlementBPM.newBuilder()
                        .setThreadID(threadId)
                        .setAutomatic(automatic)
                        .setPaymentSent(paymentSentBPM)
                        .build();
    
                return saveAndSendSettlement(appData, settlementRepository, settlementItemRepository, creditRepository, settlementBPM, settlement.get(), States.SETTLE_PAY_MADE);
            } else {
                String error = "Status is not " + States.SETTLE_PAY_AGREED.name() + " but " + settlement.get().getStatus() + ", not performing update";
                throw new Exception(error);
            }
        } else {
            String error = "Settlement not found for threadId " + threadId;
            throw new Exception(error);
        }
    }
    
    public static SettlementRest settlePaymentSentAck(AppData appData, SettlementRepository settlementRepository, SettlementItemRepository settlementItemRepository, CreditRepository creditRepository, String threadId, boolean automatic, PaymentSentBPM paymentSentBPM) throws Exception {
        log.info("settlePaymentSentAck");

        Optional<Settlement> settlement = settlementRepository.findById(threadId);

        if (settlement.isPresent()) {
            if (settlement.get().getStatus().contentEquals(States.SETTLE_PAY_MADE.name())) {
                PaymentSentAckBPM.Builder paymentSentAckBPM = PaymentSentAckBPM.newBuilder().setPaymentSent(paymentSentBPM);
                
                SettlementBPM settlementBPM = SettlementBPM.newBuilder().setThreadID(threadId)
                        .setPaymentSentAck(paymentSentAckBPM)
                        .setAutomatic(automatic).build();

                return saveAndSendSettlement(appData, settlementRepository, settlementItemRepository, creditRepository, settlementBPM, settlement.get(), States.SETTLE_PAY_ACK);
            } else {
                String error = "Status is not " + States.SETTLE_PAY_MADE.name() + " but " + settlement.get().getStatus() + ", not performing update";
                throw new Exception(error);
            }
        } else {
            String error = "Settlement not found for threadId " + threadId;
            throw new Exception(error);
        }
    }

    public static SettlementRest settlePaymentPaid(AppData appData, SettlementRepository settlementRepository, SettlementItemRepository settlementItemRepository, CreditRepository creditRepository, String threadId, boolean automatic, String additionalNotes) throws Exception {
        log.info("settlePaymentPaid");
        
        Optional<Settlement> settlement = settlementRepository.findById(threadId);

        if (settlement.isPresent()) {
            if (settlement.get().getStatus().contentEquals(States.SETTLE_PAY_ACK.name())) {
                SettlePaidBPM.Builder settlePaidBPM = SettlePaidBPM.newBuilder()
                        .setPayerName(settlement.get().getPayerName())
                        .setRecipientName(settlement.get().getRecipientName())
                        .setAdditionalNotes(additionalNotes)
                        .setPaymentReference(settlement.get().getPaymentReference())
                        .setNetValue(Utils.moneyFromSettlement(settlement.get()));
    
                SettlementBPM settlementBPM = SettlementBPM.newBuilder().setThreadID(threadId)
                        .setSettlePayment(settlePaidBPM)
                        .setAutomatic(automatic)
                        .build();
    
                return saveAndSendSettlement(appData, settlementRepository, settlementItemRepository, creditRepository, settlementBPM, settlement.get(), States.SETTLE_RCPT_REQUESTED);
            } else {
                String error = "Status is not " + States.SETTLE_PAY_ACK.name() + " but " + settlement.get().getStatus() + ", not performing update";
                throw new Exception(error);
            }
        } else {
            String error = "Settlement not found for threadId " + threadId;
            throw new Exception(error);
        }
    }
    
    public static SettlementRest settlePaymentPaidAck(AppData appData, SettlementRepository settlementRepository, SettlementItemRepository settlementItemRepository, CreditRepository creditRepository, String threadId, boolean automatic) throws Exception {
        log.info("settlePaymentPaidAck");
        
        Optional<Settlement> settlement = settlementRepository.findById(threadId);

        if (settlement.isPresent()) {
            if (settlement.get().getStatus().contentEquals(States.SETTLE_RCPT_REQUESTED.name())) {
                SettlePaidBPM.Builder settlePaidBPM = SettlePaidBPM.newBuilder()
                        .setPayerName(settlement.get().getPayerName())
                        .setRecipientName(settlement.get().getRecipientName())
                        .setAdditionalNotes(settlement.get().getAdditionalNotes())
                        .setPaymentReference(settlement.get().getPaymentReference())
                        .setNetValue(Utils.moneyFromSettlement(settlement.get()));
    
                SettlePaidAckBPM.Builder settlePaidAckBPM = SettlePaidAckBPM.newBuilder()
                        .setSettlePaid(settlePaidBPM);
                
                SettlementBPM settlementBPM = SettlementBPM.newBuilder().setThreadID(threadId)
                        .setSettlePaymentAck(settlePaidAckBPM)
                        .setAutomatic(automatic)
                        .build();
    
                return saveAndSendSettlement(appData, settlementRepository, settlementItemRepository, creditRepository, settlementBPM, settlement.get(), States.SETTLE_RCPT_CONFIRMED);
            } else {
                String error = "Status is not " + States.SETTLE_RCPT_REQUESTED.name() + " but " + settlement.get().getStatus() + ", not performing update";
                throw new Exception(error);
            }
        } else {
            String error = "Settlement not found for threadId " + threadId;
            throw new Exception(error);
        }
    }
    
    public static SettlementRest settlePaymentComplete(AppData appData, SettlementRepository settlementRepository, SettlementItemRepository settlementItemRepository, CreditRepository creditRepository, String threadId, boolean automatic, String additionalNotes) throws Exception {
        log.info("settlePaymentComplete");
        
        Optional<Settlement> settlement = settlementRepository.findById(threadId);

        if (settlement.isPresent()) {
            if (settlement.get().getStatus().contentEquals(States.SETTLE_RCPT_CONFIRMED.name())) {
                SettleCompleteBPM.Builder settleCompleteBPM = SettleCompleteBPM.newBuilder()
                        .setPayerName(settlement.get().getPayerName())
                        .setRecipientName(settlement.get().getRecipientName())
                        .setAdditionalNotes(additionalNotes)
                        .setPaymentReference(settlement.get().getPaymentReference())
                        .setNetValue(Utils.moneyFromSettlement(settlement.get()));
    
                SettlementBPM settlementBPM = SettlementBPM.newBuilder().setThreadID(threadId)
                        .setSettleComplete(settleCompleteBPM)
                        .setAutomatic(automatic)
                        .build();
    
                return saveAndSendSettlement(appData, settlementRepository, settlementItemRepository, creditRepository, settlementBPM, settlement.get(), States.SETTLE_PAY_CONFIRMED);
            } else {
                String error = "Status is not " + States.SETTLE_RCPT_CONFIRMED.name() + " but " + settlement.get().getStatus() + ", not performing update";
                throw new Exception(error);
            }
        } else {
            String error = "Settlement not found for threadId " + threadId;
            throw new Exception(error);
        }
    }

    public static SettlementRest settlePaymentCompleteAck(AppData appData, SettlementRepository settlementRepository, SettlementItemRepository settlementItemRepository, CreditRepository creditRepository, String threadId, boolean automatic, SettleCompleteBPM settleCompleteBPM) throws Exception {
        log.info("settlePaymentCompleteAck");
        
        Optional<Settlement> settlement = settlementRepository.findById(threadId);

        if (settlement.isPresent()) {
            if (settlement.get().getStatus().contentEquals(States.SETTLE_PAY_CONFIRMED.name())) {
                SettleCompleteAckBPM.Builder settleCompleteAckBPM = SettleCompleteAckBPM.newBuilder()
                        .setSettlePaid(settleCompleteBPM);
                
                SettlementBPM settlementBPM = SettlementBPM.newBuilder().setThreadID(threadId)
                        .setSettleCompleteAck(settleCompleteAckBPM)
                        .setAutomatic(automatic)
                        .build();
    
                return saveAndSendSettlement(appData, settlementRepository, settlementItemRepository, creditRepository, settlementBPM, settlement.get(), States.SETTLE_COMPLETE);
            } else {
                String error = "Status is not " + States.SETTLE_PAY_CONFIRMED.name() + " but " + settlement.get().getStatus() + ", not performing update";
                throw new Exception(error);
            }
        } else {
            String error = "Settlement not found for threadId " + threadId;
            throw new Exception(error);
        }
    }

    private static SettlementRest saveAndSendSettlement(AppData appData, SettlementRepository settlementRepository, SettlementItemRepository settlementItemRepository, CreditRepository creditRepository, SettlementBPM settlementBPM, Settlement settlement,
            States newState) throws Exception {
        
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type", "application/json");
        

        if ( ! settlement.getStatus().contentEquals(newState.name())) {
            settlement.setStatus(newState.name() + "_PENDING");
        } else {
            log.error("Settlement state is already " + newState.name());
        }

        Settlement newSettlement = settlementRepository.save(settlement);

        new OutboundHCSMessage(appData.getHCSCore()).overrideEncryptedMessages(false)
                .overrideMessageSignature(false).sendMessage(appData.getTopicIndex(), settlementBPM.toByteArray());

        log.info("Message sent successfully.");

        SettlementRest settlementResponse = new SettlementRest(newSettlement, appData, settlementItemRepository, creditRepository);
        return settlementResponse;
    }
}
