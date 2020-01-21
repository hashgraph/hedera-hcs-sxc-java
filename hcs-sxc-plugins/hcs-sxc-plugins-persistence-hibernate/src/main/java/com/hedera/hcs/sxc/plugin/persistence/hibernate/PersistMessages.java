package com.hedera.hcs.sxc.plugin.persistence.hibernate;

import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import com.hedera.hashgraph.proto.Timestamp;
import com.hedera.hashgraph.proto.TransactionBody;
import com.hedera.hashgraph.proto.mirror.ConsensusTopicResponse;
import com.hedera.hashgraph.sdk.TransactionId;
import com.hedera.hashgraph.sdk.account.AccountId;
import com.hedera.hashgraph.sdk.consensus.ConsensusMessageSubmitTransaction;
import com.hedera.hashgraph.sdk.consensus.ConsensusTopicId;
import com.hedera.hcs.sxc.plugin.persistence.entities.HCSApplicationMessage;
import com.hedera.hcs.sxc.plugin.persistence.entities.HCSTransaction;
import com.hedera.hcs.sxc.plugin.persistence.entities.MirrorResponse;
import com.hedera.hcs.sxc.interfaces.SxcMessagePersistence;
import com.hedera.hcs.sxc.commonobjects.SxcConsensusMessage;
import com.hedera.hcs.sxc.interfaces.MessagePersistenceLevel;
import com.hedera.hcs.sxc.proto.ApplicationMessage;
import com.hedera.hcs.sxc.proto.ApplicationMessageChunk;
import com.hedera.hcs.sxc.proto.ApplicationMessageId;

import lombok.extern.log4j.Log4j2;

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
implements SxcMessagePersistence{

    private final Long SCALAR = 1_000_000_000L;
    private Map<ApplicationMessageId, List<ApplicationMessageChunk>> partialMessages;
    private MessagePersistenceLevel persistenceLevel = null;
    private Map<String, String> hibernateProperties = new HashMap<String, String>();

    public PersistMessages() throws Exception {
        partialMessages = new HashMap<>();
    }

    public void setHibernateProperties(Map<String, String> hibernateProperties) {
        this.hibernateProperties = hibernateProperties;
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
    public void storeMirrorResponse(SxcConsensusMessage mirrorTopicMessageResponse) {

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
        final Session session = HibernateUtil.getHibernateSession(this.hibernateProperties);
        // start a transaction
        dbTransaction = session.beginTransaction();
        // save the student objects
        session.save(mirrorResponse);
        // commit transaction
        dbTransaction.commit();

        log.info("storeMirrorResponse " + mirrorTopicMessageResponse.toString());
    }

    @Override
    public SxcConsensusMessage getMirrorResponse(String timestamp) {

        final Session session = HibernateUtil.getHibernateSession(this.hibernateProperties);
        MirrorResponse mirrorResponse = session.createQuery("from MirrorResponse mr where mr.timestamp = :timestamp", MirrorResponse.class)
                .setParameter("timestamp", timestamp)
                .getSingleResult();

         ConsensusTopicResponse consensusTopicResponse = ConsensusTopicResponse.newBuilder()
                .setConsensusTimestamp(Timestamp.newBuilder().setSeconds(mirrorResponse.getTimestampSeconds()).setNanos(mirrorResponse.getTimestampNanos()).build())
                .setMessage(ByteString.copyFrom(mirrorResponse.getMessage()))
                .setRunningHash(ByteString.copyFrom(mirrorResponse.getRunningHash()))
                .setSequenceNumber(mirrorResponse.getSequenceNumber())
                .build();
        
        SxcConsensusMessage sxcConsensusMessage = new SxcConsensusMessage(mirrorResponse.getTopicId(), consensusTopicResponse);

        return sxcConsensusMessage;
    }

    @Override
    public Map<String, SxcConsensusMessage> getMirrorResponses() {
        Map<String, SxcConsensusMessage> responseList = new HashMap<>();

        final Session session = HibernateUtil.getHibernateSession(this.hibernateProperties);
        List < MirrorResponse > mirrorResponses = session.createQuery("from MirrorResponse mr order by mr.timestamp desc", MirrorResponse.class).list();
        mirrorResponses.forEach(mirrorResponse -> {
            ConsensusTopicResponse consensusTopicResponse = ConsensusTopicResponse.newBuilder()
                    .setConsensusTimestamp(Timestamp.newBuilder().setSeconds(mirrorResponse.getTimestampSeconds()).setNanos(mirrorResponse.getTimestampNanos()).build())
                    .setMessage(ByteString.copyFrom(mirrorResponse.getMessage()))
                    .setRunningHash(ByteString.copyFrom(mirrorResponse.getRunningHash()))
                    .setSequenceNumber(mirrorResponse.getSequenceNumber())
                    .build();

            SxcConsensusMessage consensusMessage = new SxcConsensusMessage(mirrorResponse.getTopicId(), consensusTopicResponse);

            responseList.put(mirrorResponse.getTimestamp(), consensusMessage);
        });

        return responseList;
    }

    @Override
    public Map<String, SxcConsensusMessage> getMirrorResponses(String fromTimestamp, String toTimestamp) {
        Map<String, SxcConsensusMessage> responseList = new HashMap<>();

        final Session session = HibernateUtil.getHibernateSession(this.hibernateProperties);
        List < MirrorResponse > mirrorResponses = session.createQuery("from MirrorResponse mr where mr.timestamp >= :fromTimestamp and mr.timestamp <= :toTimeStamp order by mr.timestamp desc", MirrorResponse.class)
                .setParameter("fromTimestamp", fromTimestamp)
                .setParameter("toTimestamp", toTimestamp)
                .getResultList();

        mirrorResponses.forEach(mirrorResponse -> {
            
            ConsensusTopicResponse consensusTopicResponse = ConsensusTopicResponse.newBuilder()
                    .setConsensusTimestamp(Timestamp.newBuilder().setSeconds(mirrorResponse.getTimestampSeconds()).setNanos(mirrorResponse.getTimestampNanos()).build())
                    .setMessage(ByteString.copyFrom(mirrorResponse.getMessage()))
                    .setRunningHash(ByteString.copyFrom(mirrorResponse.getRunningHash()))
                    .setSequenceNumber(mirrorResponse.getSequenceNumber())
                    .build();
            
            SxcConsensusMessage sxcConsensusMessage = new SxcConsensusMessage(mirrorResponse.getTopicId(), consensusTopicResponse);
            
            responseList.put(mirrorResponse.getTimestamp(), sxcConsensusMessage);
        });

        return responseList;
    }

    // Transactions
    @Override
    public void storeTransaction(TransactionId transactionId, ConsensusMessageSubmitTransaction submitMessageTransaction) {
        //        String txId = transactionId.accountId.shard
        //                + "." + transactionId.accountId.realm
        //                + "." + transactionId.accountId.account
        //                + "-" + transactionId.validStart.getEpochSecond()
        //                + "-" + transactionId.validStart.getNano();

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

        final Session session = HibernateUtil.getHibernateSession(this.hibernateProperties);
        HCSTransaction hcsTransaction = session.createQuery("from HCSTransaction t where t.transactionId = :transactionId", HCSTransaction.class)
                .setParameter("transactionId", transactionId)
                .getSingleResult();

        try {
          TransactionBody body = TransactionBody.parseFrom(hcsTransaction.getBodyBytes());

          ConsensusMessageSubmitTransaction tx = new ConsensusMessageSubmitTransaction();

          tx.setTransactionMemo(body.getMemo());
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
          TransactionId txId = TransactionId.withValidStart(accountId, start);
          tx.setTransactionId(txId);
          Duration validDuration = Duration.ofSeconds(body.getTransactionValidDuration().getSeconds());

          tx.setTransactionValidDuration(validDuration);

          return tx;
        } catch (InvalidProtocolBufferException e) {
            log.error(e);
            return null;
        }

    }

    @Override
    public Map<String, ConsensusMessageSubmitTransaction> getSubmittedTransactions() {
        Map<String, ConsensusMessageSubmitTransaction> responseList = new HashMap<>();

        final Session session = HibernateUtil.getHibernateSession(this.hibernateProperties);
        List<HCSTransaction> hcsTransactions = session.createQuery("from HCSTransaction t order by t.transactionId desc", HCSTransaction.class)
                .list();

        hcsTransactions.forEach(hcsTransaction -> {
            try {
                ConsensusMessageSubmitTransaction tx = new ConsensusMessageSubmitTransaction();

                TransactionBody body = TransactionBody.parseFrom(hcsTransaction.getBodyBytes());
                tx.setTransactionMemo(body.getMemo());
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
                TransactionId txId = TransactionId.withValidStart(accountId, start);
                tx.setTransactionId(txId);
                Duration validDuration = Duration.ofSeconds(body.getTransactionValidDuration().getSeconds());

                tx.setTransactionValidDuration(validDuration);

                responseList.put(hcsTransaction.getTransactionId(), tx);
            } catch (InvalidProtocolBufferException e) {
                log.error(e);
            }
        });

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
        final Session session = HibernateUtil.getHibernateSession(this.hibernateProperties);
        dbTransaction = session.beginTransaction();
        session.save(hcsApplicationMessage);
        dbTransaction.commit();

        log.info("storeApplicationMessage " + appMessageId + "-" + applicationMessage);
    }

    @Override
    public ApplicationMessage getApplicationMessage(String applicationMessageId) {
        final Session session = HibernateUtil.getHibernateSession(this.hibernateProperties);
        try {
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

        final Session session = HibernateUtil.getHibernateSession(this.hibernateProperties);
        List<HCSApplicationMessage> applicationMessages = session.createQuery("from HCSApplicationMessage", HCSApplicationMessage.class)
                .list();

        applicationMessages.forEach(applicationMessage -> {
            try {
                responseList.put(applicationMessage.getApplicationMessageId(), ApplicationMessage.parseFrom(applicationMessage.getApplicationMessage()));
            } catch (InvalidProtocolBufferException e) {
                log.error(e);
            }
        });

        return responseList;
    }

    @Override
    public void putParts(ApplicationMessageId applicationMessageId, List<ApplicationMessageChunk> l) {
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

        final Session session = HibernateUtil.getHibernateSession(this.hibernateProperties);
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
        log.info("Last consensus timestamp from database is : " + lastConsensusTimestamp.getEpochSecond() + " seconds, " + lastConsensusTimestamp.getNano() + " nanos.");

        return lastConsensusTimestamp;
    }

    @Override
    public void clear() {
        partialMessages = new HashMap<>();

        final Session session = HibernateUtil.getHibernateSession(this.hibernateProperties);
        session.beginTransaction();
        session.createQuery("delete MirrorResponse").executeUpdate();
        session.createQuery("delete HCSTransaction").executeUpdate();
        session.createQuery("delete HCSApplicationMessage").executeUpdate();
        session.getTransaction().commit();
    }
}
