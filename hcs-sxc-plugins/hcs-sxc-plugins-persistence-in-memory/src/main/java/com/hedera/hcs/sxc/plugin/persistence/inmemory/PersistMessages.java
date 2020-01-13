package com.hedera.hcs.sxc.plugin.persistence.inmemory;

import com.hedera.hashgraph.sdk.TransactionId;
import com.hedera.hashgraph.sdk.consensus.ConsensusMessage;
import com.hedera.hashgraph.sdk.consensus.ConsensusMessageSubmitTransaction;
import com.hedera.hcs.sxc.interfaces.SxcConsensusMessage;
import com.hedera.hcs.sxc.interfaces.SxcMessagePersistence;
import com.hedera.hcs.sxc.interfaces.MessagePersistenceLevel;
import com.hedera.hcs.sxc.proto.java.ApplicationMessage;
import com.hedera.hcs.sxc.proto.java.ApplicationMessageChunk;
import com.hedera.hcs.sxc.proto.java.ApplicationMessageId;

import lombok.extern.log4j.Log4j2;

import java.io.IOException;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Log4j2
public class PersistMessages 
        implements com.hedera.hcs.sxc.interfaces.SxcMessagePersistence{
    
    private Map<ApplicationMessageId, List<ApplicationMessageChunk>> partialMessages;
    private Map<String, ConsensusMessageSubmitTransaction> transactions;
    private Map<String, SxcConsensusMessage> mirrorTopicMessages;
    private Map<String, ApplicationMessage> applicationMessages;
    
    private MessagePersistenceLevel persistenceLevel = MessagePersistenceLevel.FULL;
    
    public PersistMessages() throws IOException{
        partialMessages = new HashMap<>();
        transactions = new HashMap<>();
        mirrorTopicMessages = new HashMap<>();
        applicationMessages = new HashMap<>();
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
        
        SxcConsensusMessage sxcConsensusMessage = new SxcConsensusMessage(mirrorTopicMessageResponse);
        mirrorTopicMessages.put(mirrorTopicMessageResponse.consensusTimestamp.toString(), sxcConsensusMessage);
        log.info("storeMirrorResponse " + mirrorTopicMessageResponse.consensusTimestamp.toString() + "-" + mirrorTopicMessageResponse);
    }
    
    @Override 
    public SxcConsensusMessage getMirrorResponse(String timestamp) {
        return mirrorTopicMessages.get(timestamp);
    }
        
    @Override 
    public Map<String, SxcConsensusMessage> getMirrorResponses() {
        return mirrorTopicMessages;
    }

    // Transactions
    @Override
    public void storeTransaction(TransactionId transactionId, ConsensusMessageSubmitTransaction submitMessageTransaction) {
        String txId = transactionId.accountId.shard
                + "." + transactionId.accountId.realm
                + "." + transactionId.accountId.account
                + "-" + transactionId.validStart.getEpochSecond()
                + "-" + transactionId.validStart.getNano();
        
        transactions.put(txId, submitMessageTransaction);
        log.info("storeTransaction " + txId + "-" + submitMessageTransaction);
    }
    
    @Override 
    public ConsensusMessageSubmitTransaction getSubmittedTransaction(String transactionId) {
        return transactions.get(transactionId);
    }

    @Override 
    public Map<String, ConsensusMessageSubmitTransaction> getSubmittedTransactions() {
        return transactions;
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

        applicationMessages.put(appMessageId, applicationMessage);
        log.info("storeApplicationMessage " + appMessageId + "-" + applicationMessage);
    }

    @Override
    public ApplicationMessage getApplicationMessage(String applicationMessageId) {
        return applicationMessages.get(applicationMessageId);
    }

    
    @Override
    public Map<String, ApplicationMessage> getApplicationMessages() {
        return applicationMessages;
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
        for (Map.Entry<String, SxcConsensusMessage> mirrorTopicMessage : mirrorTopicMessages.entrySet()) {
            long seconds = mirrorTopicMessage.getValue().getConsensusTimeStampSeconds();
            int nanos = mirrorTopicMessage.getValue().getConsensusTimeStampNanos();
            
            if (lastConsensusTimestamp.getEpochSecond() < seconds) {
                lastConsensusTimestamp = Instant.ofEpochSecond(seconds, nanos);
            } else if ((lastConsensusTimestamp.getEpochSecond() == seconds) && (lastConsensusTimestamp.getNano() < nanos)) {
                lastConsensusTimestamp = Instant.ofEpochSecond(seconds, nanos);
            }
        }
        return lastConsensusTimestamp;
    }

    @Override
    public void clear() {
        partialMessages = new HashMap<>();
        transactions = new HashMap<>();
        mirrorTopicMessages = new HashMap<>();
        applicationMessages = new HashMap<>();
    }
}
