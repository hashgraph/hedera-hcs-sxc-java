package com.hedera.plugin.persistence.db;

import com.google.protobuf.InvalidProtocolBufferException;
import com.hedera.hashgraph.sdk.Client;
import com.hedera.hashgraph.sdk.TransactionId;
import com.hedera.hashgraph.sdk.account.AccountId;
import com.hedera.hashgraph.sdk.consensus.SubmitMessageTransaction;
import com.hedera.hashgraph.sdk.consensus.TopicId;
import com.hedera.hashgraph.sdk.proto.TransactionBody;
import com.hedera.hcslib.interfaces.LibMessagePersistence;
import com.hedera.hcslib.interfaces.MessagePersistenceLevel;
import com.hedera.hcslib.proto.java.ApplicationMessage;
import com.hedera.hcslib.proto.java.ApplicationMessageChunk;
import com.hedera.hcslib.proto.java.ApplicationMessageId;
import com.hedera.mirror.api.proto.java.MirrorGetTopicMessages.MirrorGetTopicMessagesResponse;
import com.hedera.plugin.persistence.config.Config;
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

import org.hibernate.Session;
import org.hibernate.Transaction;

@Log4j2
public class PersistMessages 
        implements LibMessagePersistence{
    
    private Map<ApplicationMessageId, List<ApplicationMessageChunk>> partialMessages;
    
    private Config config = null;
    private MessagePersistenceLevel persistenceLevel = null;
    
    public PersistMessages() throws IOException{
        config = new Config();
        persistenceLevel = config.getConfig().getPersistenceLevel();
        partialMessages = new HashMap<>();
    }

//    0: none
//    1: timestamp, hash, signature and content for my messages (those I sent or those sent to me)
//    2: 1+ timestamps, hashes and signatures for all messages (regardless of sender/recipient), and content only for my messages
//    3: timestamp, hash, signature and contents for all messages
//  
    // Mirror responses
    @Override
    public void storeMirrorResponse(MirrorGetTopicMessagesResponse mirrorTopicMessageResponse) {
        String timestamp = mirrorTopicMessageResponse.getConsensusTimestamp().getSeconds() + "." + mirrorTopicMessageResponse.getConsensusTimestamp().getNanos();
        
        MirrorResponse mirrorResponse = new MirrorResponse();
        mirrorResponse.setMirrorTopicMessageResponse(mirrorTopicMessageResponse.toByteArray());
        mirrorResponse.setTimestamp(timestamp);
        
        Transaction dbTransaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            // start a transaction
            dbTransaction = session.beginTransaction();
            // save the student objects
            session.save(mirrorResponse);
            // commit transaction
            dbTransaction.commit();

            log.info("storeMirrorResponse " + timestamp + "-" + mirrorTopicMessageResponse);
        } catch (Exception e) {
            if (dbTransaction != null) {
                dbTransaction.rollback();
            }
            e.printStackTrace();
        }
    }
    
    @Override 
    public MirrorGetTopicMessagesResponse getMirrorResponse(String timestamp) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            MirrorResponse mirrorResponse = session.createQuery("from MirrorResponse mr where mr.timestamp = :timestamp", MirrorResponse.class)
                    .setParameter("timestamp", timestamp)
                    .getSingleResult();
            return MirrorGetTopicMessagesResponse.parseFrom(mirrorResponse.getMirrorTopicMessageResponse());
        } catch (InvalidProtocolBufferException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return null;
    }
        
    @Override 
    public Map<String, MirrorGetTopicMessagesResponse> getMirrorResponses() {
        Map<String, MirrorGetTopicMessagesResponse> responseList = new HashMap<>();
        
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            List < MirrorResponse > mirrorResponses = session.createQuery("from MirrorResponse", MirrorResponse.class).list();
            mirrorResponses.forEach(mirrorResponse -> {
                    try {
                        responseList.put(mirrorResponse.getTimestamp(), MirrorGetTopicMessagesResponse.parseFrom(mirrorResponse.getMirrorTopicMessageResponse()));
                    } catch (InvalidProtocolBufferException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
            });
        }
        
        return responseList;
     }

    // Transactions
    @Override
    public void storeTransaction(TransactionId transactionId, SubmitMessageTransaction submitMessageTransaction) {
        String txId = transactionId.getAccountId().getShardNum()
                + "." + transactionId.getAccountId().getRealmNum()
                + "." + transactionId.getAccountId().getAccountNum()
                + "-" + transactionId.getValidStart().getEpochSecond()
                + "-" + transactionId.getValidStart().getNano();
        
        HCSTransaction hcsTransaction = new HCSTransaction();

        hcsTransaction.setBodyBytes(submitMessageTransaction.toBytes(false));
        hcsTransaction.setTransactionId(txId);
        
        Transaction dbTransaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            // start a transaction
            dbTransaction = session.beginTransaction();
            // save the student objects
            session.save(hcsTransaction);
            // commit transaction
            dbTransaction.commit();

            log.info("storeTransaction " + txId + "-" + submitMessageTransaction);
        } catch (Exception e) {
            if (dbTransaction != null) {
                dbTransaction.rollback();
            }
            e.printStackTrace();
        }
    }
    
    @Override 
    public SubmitMessageTransaction getSubmittedTransaction(String transactionId) {
        
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            HCSTransaction hcsTransaction = session.createQuery("from HCSTransaction t where t.transactionId = :transactionId", HCSTransaction.class)
                    .setParameter("transactionId", transactionId)
                    .getSingleResult();
            
            TransactionBody body = TransactionBody.parseFrom(hcsTransaction.getBodyBytes());

            Client client = null;
            SubmitMessageTransaction tx = new SubmitMessageTransaction(client);
            
            tx.setMemo(body.getMemo());
            tx.setMessage(body.getConsensusSubmitMessage().getMessage().toByteArray());
            AccountId accountId = new AccountId(body.getNodeAccountID().getShardNum()
                    ,body.getNodeAccountID().getRealmNum()
                    ,body.getNodeAccountID().getAccountNum()
            );
            tx.setNodeAccountId(accountId);
            
            TopicId topicId = new TopicId(body.getConsensusSubmitMessage().getTopicID().getShardNum()
                    ,body.getConsensusSubmitMessage().getTopicID().getRealmNum()
                    ,body.getConsensusSubmitMessage().getTopicID().getTopicNum()
            );
            
            tx.setTopicId(topicId);
            
            tx.setTransactionFee(body.getTransactionFee());
            
            Instant start = Instant.ofEpochSecond(body.getTransactionID().getTransactionValidStart().getSeconds(), body.getTransactionID().getTransactionValidStart().getNanos());
            TransactionId txId = new TransactionId(accountId, start);
            tx.setTransactionId(txId);
            Duration validDuration = Duration.ofSeconds(body.getTransactionValidDuration().getSeconds());
            
            tx.setTransactionValidDuration(validDuration);
          
            return tx;
        } catch (InvalidProtocolBufferException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return null;
        
    }

    @Override 
    public Map<String, SubmitMessageTransaction> getSubmittedTransactions() {
        Map<String, SubmitMessageTransaction> responseList = new HashMap<>();

        try (Session session = HibernateUtil.getSessionFactory().openSession()) {

            List<HCSTransaction> hcsTransactions = session.createQuery("from HCSTransaction", HCSTransaction.class)
                    .list();
            
            hcsTransactions.forEach(hcsTransaction -> {
                try {
                    SubmitMessageTransaction tx = new SubmitMessageTransaction(null);

                    TransactionBody body = TransactionBody.parseFrom(hcsTransaction.getBodyBytes());
                    tx.setMemo(body.getMemo());
                    tx.setMessage(body.getConsensusSubmitMessage().getMessage().toByteArray());
                    AccountId accountId = new AccountId(body.getNodeAccountID().getShardNum()
                            ,body.getNodeAccountID().getRealmNum()
                            ,body.getNodeAccountID().getAccountNum()
                    );
                    tx.setNodeAccountId(accountId);
                    
                    TopicId topicId = new TopicId(body.getConsensusSubmitMessage().getTopicID().getShardNum()
                            ,body.getConsensusSubmitMessage().getTopicID().getRealmNum()
                            ,body.getConsensusSubmitMessage().getTopicID().getTopicNum()
                    );
                    
                    tx.setTopicId(topicId);
                    
                    tx.setTransactionFee(body.getTransactionFee());
                    
                    Instant start = Instant.ofEpochSecond(body.getTransactionID().getTransactionValidStart().getSeconds(), body.getTransactionID().getTransactionValidStart().getNanos());
                    TransactionId txId = new TransactionId(accountId, start);
                    tx.setTransactionId(txId);
                    Duration validDuration = Duration.ofSeconds(body.getTransactionValidDuration().getSeconds());
                    
                    tx.setTransactionValidDuration(validDuration);
                    
                    responseList.put(hcsTransaction.getTransactionId(), tx);
                } catch (InvalidProtocolBufferException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
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
            // start a transaction
            dbTransaction = session.beginTransaction();
            // save the student objects
            session.save(hcsApplicationMessage);
            // commit transaction
            dbTransaction.commit();

            log.info("storeApplicationMessage " + appMessageId + "-" + applicationMessage);

        } catch (Exception e) {
            if (dbTransaction != null) {
                dbTransaction.rollback();
            }
            e.printStackTrace();
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
            // TODO Auto-generated catch block
            e.printStackTrace();
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
                    // TODO Auto-generated catch block
                    e.printStackTrace();
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
    public void clear() {
        partialMessages = new HashMap<>();

        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            session.createQuery("delete from MirrorResponse", MirrorResponse.class)
                .executeUpdate();
            session.createQuery("delete from HCSTransaction", MirrorResponse.class)
                .executeUpdate();
            session.createQuery("delete from HCSApplicationMessage", MirrorResponse.class)
                .executeUpdate();
        }
    }
}
