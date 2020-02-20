package com.hedera.hcs.sxc.plugin.persistence.hibernate;

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
import com.hedera.hcs.sxc.interfaces.SxcPersistence;
import com.hedera.hcs.sxc.commonobjects.SxcConsensusMessage;
import com.hedera.hcs.sxc.interfaces.MessagePersistenceLevel;
import com.hedera.hcs.sxc.interfaces.SXCApplicationMessageInterface;
import com.hedera.hcs.sxc.plugin.persistence.entities.KeyStore;
import com.hedera.hcs.sxc.proto.ApplicationMessage;
import com.hedera.hcs.sxc.proto.ApplicationMessageChunk;
import com.hedera.hcs.sxc.proto.ApplicationMessageID;

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
public class Persist
implements SxcPersistence{

    private final Long SCALAR = 1_000_000_000L;
    private Map<ApplicationMessageID, List<ApplicationMessageChunk>> partialMessages;
    private MessagePersistenceLevel persistenceLevel = null;
    private Map<String, String> hibernateProperties = new HashMap<String, String>();

    public Persist() throws Exception {
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

        Session session = HibernateUtil.getHibernateSession(this.hibernateProperties);

        MirrorResponse mirrorResponse = session.createQuery("from MirrorResponse mr where mr.timestamp = :timestamp", MirrorResponse.class)
                .setParameter("timestamp", mirrorTopicMessageResponse.consensusTimestamp.toString())
                .getResultList()
                .stream().findFirst().orElse(null);
        if (mirrorResponse == null) {
            mirrorResponse = new MirrorResponse();
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
            // start a transaction
            dbTransaction = session.beginTransaction();
            
            session.save(mirrorResponse);
            // commit transaction
            session.flush();
            dbTransaction.commit();
    
            log.debug("storeMirrorResponse " + mirrorTopicMessageResponse.toString());
        } else {
            log.debug("Skipping duplicate mirror response entry");
        }
        session.close();
    }

    @Override
    public SxcConsensusMessage getMirrorResponse(String timestamp) {

        Session session = HibernateUtil.getHibernateSession(this.hibernateProperties);
        MirrorResponse mirrorResponse = session.createQuery("from MirrorResponse mr where mr.timestamp = :timestamp", MirrorResponse.class)
                .setParameter("timestamp", timestamp)
                .getResultList()
                .stream().findFirst().orElse(null);
        if (mirrorResponse == null) {
            return null;
        }

         ConsensusTopicResponse consensusTopicResponse = ConsensusTopicResponse.newBuilder()
                .setConsensusTimestamp(Timestamp.newBuilder().setSeconds(mirrorResponse.getTimestampSeconds()).setNanos(mirrorResponse.getTimestampNanos()).build())
                .setMessage(ByteString.copyFrom(mirrorResponse.getMessage()))
                .setRunningHash(ByteString.copyFrom(mirrorResponse.getRunningHash()))
                .setSequenceNumber(mirrorResponse.getSequenceNumber())
                .build();
        
        SxcConsensusMessage sxcConsensusMessage = new SxcConsensusMessage(mirrorResponse.getTopicId(), consensusTopicResponse);
        session.close();
        return sxcConsensusMessage;
    }

    @Override
    public Map<String, SxcConsensusMessage> getMirrorResponses() {
        Map<String, SxcConsensusMessage> responseList = new HashMap<>();

        Session session = HibernateUtil.getHibernateSession(this.hibernateProperties);
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
        session.close();
        return responseList;
    }

    @Override
    public Map<String, SxcConsensusMessage> getMirrorResponses(String fromTimestamp, String toTimestamp) {
        Map<String, SxcConsensusMessage> responseList = new HashMap<>();

        Session session = HibernateUtil.getHibernateSession(this.hibernateProperties);
        List < MirrorResponse > mirrorResponses = session.createQuery("from MirrorResponse mr where mr.timestamp >= :fromTimestamp and mr.timestamp <= :toTimestamp order by mr.timestamp desc", MirrorResponse.class)
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
        session.close();
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
        //            log.debug("storeTransaction " + txId + "-" + submitMessageTransaction);
        //        } catch (Exception e) {
        //            if (dbTransaction != null) {
        //                dbTransaction.rollback();
        //            }
        //            log.error(e);
        //        }
    }

    @Override
    public ConsensusMessageSubmitTransaction getSubmittedTransaction(String transactionId) {

        Session session = HibernateUtil.getHibernateSession(this.hibernateProperties);
        HCSTransaction hcsTransaction = session.createQuery("from HCSTransaction t where t.transactionId = :transactionId", HCSTransaction.class)
                .setParameter("transactionId", transactionId)
                .getResultList()
                .stream().findFirst().orElse(null);
        if (hcsTransaction == null) {
            session.close();
            return null;
        }

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
          session.close();
          return tx;
        } catch (InvalidProtocolBufferException e) {
            log.error(e);
            session.close();
            return null;
        }
        //session.close();
    }

    @Override
    public Map<String, ConsensusMessageSubmitTransaction> getSubmittedTransactions() {
        Map<String, ConsensusMessageSubmitTransaction> responseList = new HashMap<>();

        Session session = HibernateUtil.getHibernateSession(this.hibernateProperties);
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

        session.close();
        return responseList;

    }

    @Override
    public List<ApplicationMessageChunk> getParts(ApplicationMessageID applicationMessageId) {
        return this.partialMessages.get(applicationMessageId);
    }

    @Override
    public void storeApplicationMessage(
            ApplicationMessage applicationMessage,
            Instant lastChronoPartConsensusTimestamp,
            String lastChronoPartRunningHash,
            long lastChronoPartSequenceNum
    
    ) {
        ApplicationMessageID applicationMessageId = applicationMessage.getApplicationMessageId();
        String appMessageId = applicationMessageId.getAccountID().getShardNum()
                + "." + applicationMessageId.getAccountID().getRealmNum()
                + "." + applicationMessageId.getAccountID().getAccountNum()
                + "-" + applicationMessageId.getValidStart().getSeconds()
                + "-" + applicationMessageId.getValidStart().getNanos();

        Session session = HibernateUtil.getHibernateSession(this.hibernateProperties);
        HCSApplicationMessage hcsApplicationMessage = session.createQuery("from HCSApplicationMessage m where m.applicationMessageId = :applicationMessageId", HCSApplicationMessage.class)
                .setParameter("applicationMessageId", appMessageId)
                .getResultList()
                .stream().findFirst().orElse(null);
        
        if (hcsApplicationMessage == null) {
            hcsApplicationMessage = new HCSApplicationMessage();

            hcsApplicationMessage.setApplicationMessageId(appMessageId);
            hcsApplicationMessage.setApplicationMessage(applicationMessage.toByteArray());
            hcsApplicationMessage.setLastChronoPartConsensusTimestamp(lastChronoPartConsensusTimestamp);
            hcsApplicationMessage.setLastChronoPartRunningHashHEX(lastChronoPartRunningHash);
            hcsApplicationMessage.setLastChronoPartSequenceNum(lastChronoPartSequenceNum);
            Transaction dbTransaction = null;
            dbTransaction = session.beginTransaction();
            session.save(hcsApplicationMessage);
            session.flush();
            dbTransaction.commit();
    
            log.debug("storeApplicationMessage " + appMessageId + "-" + applicationMessage);
        } else {
            log.debug("Application message already in database");
        }
        session.close();
    }
    
   

    @Override
    public ApplicationMessage getApplicationMessage(String applicationMessageId) {
        Session session = HibernateUtil.getHibernateSession(this.hibernateProperties);
        try {
            HCSApplicationMessage applicationMessage = session.createQuery("from HCSApplicationMessage m where m.applicationMessageId = :applicationMessageId", HCSApplicationMessage.class)
                    .setParameter("applicationMessageId", applicationMessageId)
                    .getResultList()
                    .stream().findFirst().orElse(null);
            if (applicationMessage == null) {
                session.close();
                return null;
            }
            session.close();
            return ApplicationMessage.parseFrom(applicationMessage.getApplicationMessage());
        } catch (InvalidProtocolBufferException e) {
            log.error(e);
        }
        session.close();
        return null;
    }
    
    @Override
    public SXCApplicationMessageInterface getApplicationMessageEntity(String applicationMessageId) {
        Session session = HibernateUtil.getHibernateSession(this.hibernateProperties);
        HCSApplicationMessage applicationMessage = session.createQuery("from HCSApplicationMessage m where m.applicationMessageId = :applicationMessageId", HCSApplicationMessage.class)
                .setParameter("applicationMessageId", applicationMessageId)
                .getResultList()
                .stream().findFirst().orElse(null);
        if (applicationMessage == null) {
            session.close();
            return null;
        }
        session.close();
        return applicationMessage;
    }


    @Override
    public Map<String, ApplicationMessage> getApplicationMessages() {
        Map<String, ApplicationMessage> responseList = new HashMap<>();

        Session session = HibernateUtil.getHibernateSession(this.hibernateProperties);
        List<HCSApplicationMessage> applicationMessages = session.createQuery("from HCSApplicationMessage", HCSApplicationMessage.class)
                .list();

        applicationMessages.forEach(applicationMessage -> {
            try {
                responseList.put(applicationMessage.getApplicationMessageId(), ApplicationMessage.parseFrom(applicationMessage.getApplicationMessage()));
            } catch (InvalidProtocolBufferException e) {
                log.error(e);
            }
        });

        session.close();
        return responseList;
    }
    
    
    @Override
    public List<? extends SXCApplicationMessageInterface> getSXCApplicationMessages() {
         
        Session session = HibernateUtil.getHibernateSession(this.hibernateProperties);
        List<HCSApplicationMessage> applicationMessages = session.createQuery("from HCSApplicationMessage", HCSApplicationMessage.class)
                .list();
        session.close();
        return applicationMessages;
    }

    
    @Override
    public void putParts(ApplicationMessageID applicationMessageId, List<ApplicationMessageChunk> l) {
        // always keep data to allow for reassembly of messages,
        // part messages can be deleted once full messages have been reconstituted
        // see removeParts
        this.partialMessages.put(applicationMessageId, l);
    }

    @Override
    public void removeParts(ApplicationMessageID applicationMessageId) {
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

        Session session = HibernateUtil.getHibernateSession(this.hibernateProperties);
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
        log.debug("Last consensus timestamp from database is : " + lastConsensusTimestamp.getEpochSecond() + " seconds, " + lastConsensusTimestamp.getNano() + " nanos.");

        session.close();
        return lastConsensusTimestamp;
    }

    @Override
    public void clear() {
        partialMessages = new HashMap<>();

        Session session = HibernateUtil.getHibernateSession(this.hibernateProperties);
        session.beginTransaction();
        session.createQuery("delete MirrorResponse").executeUpdate();
        session.createQuery("delete HCSTransaction").executeUpdate();
        session.createQuery("delete HCSApplicationMessage").executeUpdate();
        session.flush();
        session.getTransaction().commit();
        session.close();
    }

    @Override
    public void storeSecretKey(byte[] secretKey) {
        Session session = HibernateUtil.getHibernateSession(this.hibernateProperties);
        List<KeyStore> resultList = session.createQuery("select k from KeyStore k").getResultList();
        if (resultList.size() == 1){
            KeyStore ks = resultList.get(0);
            ks.setId(0);
            ks.setSecretKey(secretKey);
            session.update(ks);
        } else {
            KeyStore ks = new KeyStore();
            ks.setId(0);
            ks.setSecretKey(secretKey);
            session.save(ks);
        }
        session.close();
    }

    @Override
    public byte[] getSecretKey() {
        Session session = HibernateUtil.getHibernateSession(this.hibernateProperties);
        KeyStore ks = session.find(KeyStore.class, 0);
        session.close();
        return ks.getSecretKey();
    }   

    @Override
    public void storePublicKey(byte[] publicKey) {
        Session session = HibernateUtil.getHibernateSession(this.hibernateProperties);
        KeyStore ks = session.find(KeyStore.class, 0);
        ks.setPublicKey(publicKey);
        session.save(ks);
        session.close();
    }

    @Override
    public byte[] getPublicKey() {
        
        Session session = HibernateUtil.getHibernateSession(this.hibernateProperties);
        KeyStore ks = session.find(KeyStore.class, 0);
        session.close();
        return ks.getPublicKey();
    }
 
}
