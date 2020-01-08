package com.hedera.plugin.persistence.db;

import com.google.protobuf.InvalidProtocolBufferException;
import com.hedera.hashgraph.proto.TransactionBody;
import com.hedera.hashgraph.sdk.Client;
import com.hedera.hashgraph.sdk.TransactionId;
import com.hedera.hashgraph.sdk.account.AccountId;
import com.hedera.hashgraph.sdk.consensus.ConsensusMessage;
import com.hedera.hashgraph.sdk.consensus.ConsensusMessageSubmitTransaction;
import com.hedera.hashgraph.sdk.consensus.ConsensusTopicId;
import com.hedera.hcslib.interfaces.LibConsensusMessage;
import com.hedera.hcslib.interfaces.LibMessagePersistence;
import com.hedera.hcslib.interfaces.MessagePersistenceLevel;
import com.hedera.hcslib.proto.java.ApplicationMessage;
import com.hedera.hcslib.proto.java.ApplicationMessageChunk;
import com.hedera.hcslib.proto.java.ApplicationMessageId;
import com.hedera.plugin.persistence.entities.HCSApplicationMessage;
import com.hedera.plugin.persistence.entities.HCSTransaction;
import com.hedera.plugin.persistence.entities.MirrorResponse;

import lombok.extern.log4j.Log4j2;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.hibernate.query.Query;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

import org.hibernate.Session;
import org.hibernate.Transaction;

@Log4j2
public class PersistMessages
        implements LibMessagePersistence{

    private final Long SCALAR = 1_000_000_000L;

    private Map<ApplicationMessageId, List<ApplicationMessageChunk>> partialMessages;

    private MessagePersistenceLevel persistenceLevel = null;

    public PersistMessages() throws IOException{
        partialMessages = new HashMap<>();
    }

    public void setPersistenceLevel(MessagePersistenceLevel persistenceLevel) {
        this.persistenceLevel = persistenceLevel;
    }
//    0: none
//    1: timestamp, hash, signature and content for my messages (those I sent or those sent to me)
//    2: 1+ timestamps, hashes and signatures for all messages (regardless of sender/recipient), and content only for my messages
//    3: timestamp, hash, signature and contents for all messages
//
    // Mirror responses
    @Override
    public void storeMirrorResponse(ConsensusMessage mirrorTopicMessageResponse) {
        
        MirrorResponse mirrorResponse = new MirrorResponse();

        mirrorResponse.setMessage(mirrorTopicMessageResponse.message);
        mirrorResponse.setRunningHash(mirrorTopicMessageResponse.runningHash);
        mirrorResponse.setSequenceNumber(mirrorTopicMessageResponse.sequenceNumber);
        mirrorResponse.setTopicId(mirrorTopicMessageResponse.topicId.toString());
        mirrorResponse.setTimestamp(mirrorTopicMessageResponse.consensusTimestamp.toString());
        mirrorResponse.setTimestampSeconds(mirrorTopicMessageResponse.consensusTimestamp.getEpochSecond());
        mirrorResponse.setTimestampNanos(mirrorTopicMessageResponse.consensusTimestamp.getNano());
        long timestampNS = mirrorTopicMessageResponse.consensusTimestamp.getEpochSecond() * SCALAR + mirrorTopicMessageResponse.consensusTimestamp.getNano();
        mirrorResponse.setTimestampNS(timestampNS);

        Transaction dbTransaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            // start a transaction
            dbTransaction = session.beginTransaction();
            // save the student objects
            session.save(mirrorResponse);
            // commit transaction
            dbTransaction.commit();

            log.info("storeMirrorResponse " + mirrorTopicMessageResponse.toString());
        } catch (Exception e) {
            if (dbTransaction != null) {
                dbTransaction.rollback();
            }
            log.error(e);
        }
    }

    @Override
    public LibConsensusMessage getMirrorResponse(String timestamp) {

        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            MirrorResponse mirrorResponse = session.createQuery("from MirrorResponse mr where mr.timestamp = :timestamp", MirrorResponse.class)
                    .setParameter("timestamp", timestamp)
                    .getSingleResult();

            LibConsensusMessage libConsensusMessage = new LibConsensusMessage();
            libConsensusMessage.setConsensusTimeStampSeconds(mirrorResponse.getTimestampSeconds());
            libConsensusMessage.setConsensusTimeStampNanos(mirrorResponse.getTimestampNanos());
            libConsensusMessage.setMessage(mirrorResponse.getMessage());
            libConsensusMessage.setRunningHash(mirrorResponse.getRunningHash());
            libConsensusMessage.setSequenceNumber(mirrorResponse.getSequenceNumber());
            libConsensusMessage.setTopicId(mirrorResponse.getTopicId());

            return libConsensusMessage;
        }
    }

    @Override
    public Map<String, LibConsensusMessage> getMirrorResponses() {
        Map<String, LibConsensusMessage> responseList = new HashMap<>();

        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            List < MirrorResponse > mirrorResponses = session.createQuery("from MirrorResponse", MirrorResponse.class).list();
            mirrorResponses.forEach(mirrorResponse -> {
                LibConsensusMessage libConsensusMessage = new LibConsensusMessage();
                libConsensusMessage.setConsensusTimeStampSeconds(mirrorResponse.getTimestampSeconds());
                libConsensusMessage.setConsensusTimeStampNanos(mirrorResponse.getTimestampNanos());
                libConsensusMessage.setMessage(mirrorResponse.getMessage());
                libConsensusMessage.setRunningHash(mirrorResponse.getRunningHash());
                libConsensusMessage.setSequenceNumber(mirrorResponse.getSequenceNumber());
                libConsensusMessage.setTopicId(mirrorResponse.getTopicId());

                responseList.put(mirrorResponse.getTimestamp(), libConsensusMessage);
            });
        }

        return responseList;
     }

    // Transactions
    @Override
    public void storeTransaction(TransactionId transactionId, ConsensusMessageSubmitTransaction submitMessageTransaction) {
        String txId = transactionId.accountId.shard
                + "." + transactionId.accountId.realm
                + "." + transactionId.accountId.account
                + "-" + transactionId.validStart.getEpochSecond()
                + "-" + transactionId.validStart.getNano();

//        HCSTransaction hcsTransaction = new HCSTransaction();
//
//        hcsTransaction.setBodyBytes(submitMessageTransaction.build().toBytes(false));
//        hcsTransaction.setTransactionId(txId);
//
//        Transaction dbTransaction = null;
//        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
//            // start a transaction
//            dbTransaction = session.beginTransaction();
//            // save the student objects
//            session.save(hcsTransaction);
//            // commit transaction
//            dbTransaction.commit();
//
//            log.info("storeTransaction " + txId + "-" + submitMessageTransaction);
//        } catch (Exception e) {
//            if (dbTransaction != null) {
//                dbTransaction.rollback();
//            }
//            log.error(e);
//        }
    }

    @Override
    public ConsensusMessageSubmitTransaction getSubmittedTransaction(String transactionId) {

        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            HCSTransaction hcsTransaction = session.createQuery("from HCSTransaction t where t.transactionId = :transactionId", HCSTransaction.class)
                    .setParameter("transactionId", transactionId)
                    .getSingleResult();

            TransactionBody body = TransactionBody.parseFrom(hcsTransaction.getBodyBytes());

            Client client = null;
            ConsensusMessageSubmitTransaction tx = new ConsensusMessageSubmitTransaction();

            tx.setMemo(body.getMemo());
            tx.setMessage(body.getConsensusSubmitMessage().getMessage().toByteArray());
            AccountId accountId = new AccountId(body.getNodeAccountID().getShardNum()
                    ,body.getNodeAccountID().getRealmNum()
                    ,body.getNodeAccountID().getAccountNum()
            );
            tx.setNodeAccountId(accountId);

            ConsensusTopicId topicId = new ConsensusTopicId(body.getConsensusSubmitMessage().getTopicID().getShardNum()
                    ,body.getConsensusSubmitMessage().getTopicID().getRealmNum()
                    ,body.getConsensusSubmitMessage().getTopicID().getTopicNum()
            );

            tx.setTopicId(topicId);

            tx.setMaxTransactionFee(body.getTransactionFee());

            Instant start = Instant.ofEpochSecond(body.getTransactionID().getTransactionValidStart().getSeconds(), body.getTransactionID().getTransactionValidStart().getNanos());
            TransactionId txId = new TransactionId(accountId, start);
            tx.setTransactionId(txId);
            Duration validDuration = Duration.ofSeconds(body.getTransactionValidDuration().getSeconds());

            tx.setTransactionValidDuration(validDuration);

            return tx;
        } catch (InvalidProtocolBufferException e) {
            log.error(e);
        }
        return null;

    }

    @Override
    public Map<String, ConsensusMessageSubmitTransaction> getSubmittedTransactions() {
        Map<String, ConsensusMessageSubmitTransaction> responseList = new HashMap<>();

        try (Session session = HibernateUtil.getSessionFactory().openSession()) {

            List<HCSTransaction> hcsTransactions = session.createQuery("from HCSTransaction", HCSTransaction.class)
                    .list();

            hcsTransactions.forEach(hcsTransaction -> {
                try {
                    ConsensusMessageSubmitTransaction tx = new ConsensusMessageSubmitTransaction();

                    TransactionBody body = TransactionBody.parseFrom(hcsTransaction.getBodyBytes());
                    tx.setMemo(body.getMemo());
                    tx.setMessage(body.getConsensusSubmitMessage().getMessage().toByteArray());
                    AccountId accountId = new AccountId(body.getNodeAccountID().getShardNum()
                            ,body.getNodeAccountID().getRealmNum()
                            ,body.getNodeAccountID().getAccountNum()
                    );
                    tx.setNodeAccountId(accountId);

                    ConsensusTopicId topicId = new ConsensusTopicId(body.getConsensusSubmitMessage().getTopicID().getShardNum()
                            ,body.getConsensusSubmitMessage().getTopicID().getRealmNum()
                            ,body.getConsensusSubmitMessage().getTopicID().getTopicNum()
                    );

                    tx.setTopicId(topicId);

                    tx.setMaxTransactionFee(body.getTransactionFee());

                    Instant start = Instant.ofEpochSecond(body.getTransactionID().getTransactionValidStart().getSeconds(), body.getTransactionID().getTransactionValidStart().getNanos());
                    TransactionId txId = new TransactionId(accountId, start);
                    tx.setTransactionId(txId);
                    Duration validDuration = Duration.ofSeconds(body.getTransactionValidDuration().getSeconds());

                    tx.setTransactionValidDuration(validDuration);

                    responseList.put(hcsTransaction.getTransactionId(), tx);
                } catch (InvalidProtocolBufferException e) {
                    log.error(e);
                }
            });

        }
        return responseList;

    }

    @Override
    public List<ApplicationMessageChunk> getParts(ApplicationMessageId applicationMessageId) {
        return this.partialMessages.get(applicationMessageId);
    }

    @Override
    public void storeApplicationMessage(ApplicationMessageId applicationMessageId, ApplicationMessage applicationMessage) {
        String appMessageId = applicationMessageId.getAccountID().getShardNum()
                + "." + applicationMessageId.getAccountID().getRealmNum()
                + "." + applicationMessageId.getAccountID().getAccountNum()
                + "-" + applicationMessageId.getValidStart().getSeconds()
                + "-" + applicationMessageId.getValidStart().getNanos();

        HCSApplicationMessage hcsApplicationMessage = new HCSApplicationMessage();

        hcsApplicationMessage.setApplicationMessageId(appMessageId);
        hcsApplicationMessage.setApplicationMessage(applicationMessage.toByteArray());

        Transaction dbTransaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            dbTransaction = session.beginTransaction();
            session.save(hcsApplicationMessage);
            dbTransaction.commit();

            log.info("storeApplicationMessage " + appMessageId + "-" + applicationMessage);

        } catch (Exception e) {
            if (dbTransaction != null) {
                dbTransaction.rollback();
            }
            log.error(e);
        }
    }

    @Override
    public ApplicationMessage getApplicationMessage(String applicationMessageId) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            HCSApplicationMessage applicationMessage = session.createQuery("from HCSApplicationMessage m where m.applicationMessageId = :applicationMessageId", HCSApplicationMessage.class)
                    .setParameter("applicationMessageId", applicationMessageId)
                    .getSingleResult();
            return ApplicationMessage.parseFrom(applicationMessage.getApplicationMessage());
        } catch (InvalidProtocolBufferException e) {
            log.error(e);
        }
        return null;
    }


    @Override
    public Map<String, ApplicationMessage> getApplicationMessages() {
        Map<String, ApplicationMessage> responseList = new HashMap<>();

        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            List<HCSApplicationMessage> applicationMessages = session.createQuery("from HCSApplicationMessage", HCSApplicationMessage.class)
                    .list();

            applicationMessages.forEach(applicationMessage -> {
                try {
                    responseList.put(applicationMessage.getApplicationMessageId(), ApplicationMessage.parseFrom(applicationMessage.getApplicationMessage()));
                } catch (InvalidProtocolBufferException e) {
                    log.error(e);
                }
            });
        }

        return responseList;
    }

    @Override
    public void putParts(ApplicationMessageId applicationMessageId, List l) {
        // always keep data to allow for reassembly of messages,
        // part messages can be deleted once full messages have been reconstituted
        // see removeParts
        this.partialMessages.put(applicationMessageId, l);
    }

    @Override
    public void removeParts(ApplicationMessageId applicationMessageId) {
        switch (persistenceLevel) {
            case FULL:
                // do not remove stored data
                break;
            case MESSAGE_AND_PARTS:
                // do not remove stored data
                break;
            case MESSAGE_ONLY:
                this.partialMessages.remove(applicationMessageId);
                break;
            case NONE:
                this.partialMessages.remove(applicationMessageId);
                break;
        }
    }

    @Override
    public Instant getLastConsensusTimestamp() {

        Instant lastConsensusTimestamp = Instant.EPOCH;

        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            CriteriaBuilder builder = session.getCriteriaBuilder();
            CriteriaQuery<Long> criteriaQuery = builder.createQuery(Long.class);
            
            Root<MirrorResponse> root = criteriaQuery.from(MirrorResponse.class);
            criteriaQuery.select(builder.max(root.get("timestampNS")));
            
            Query<Long> query = session.createQuery(criteriaQuery);
            Long maxTimestamp = query.getSingleResult();
            
            if (maxTimestamp == null) {
                lastConsensusTimestamp = Instant.EPOCH;
            } else {
                long seconds = maxTimestamp / SCALAR;
                int nanos = (int) (maxTimestamp % SCALAR);
                lastConsensusTimestamp = Instant.ofEpochSecond(seconds, nanos);
            }
        }

        return lastConsensusTimestamp;
    }

    @Override
    public void clear() {
        partialMessages = new HashMap<>();

        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            session.beginTransaction();
            session.createQuery("delete MirrorResponse").executeUpdate();
            session.createQuery("delete HCSTransaction").executeUpdate();
            session.createQuery("delete HCSApplicationMessage").executeUpdate();
            session.getTransaction().commit();
        }
    }
}
