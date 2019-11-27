package com.hedera.plugin.persistence.inmemory;

import com.hedera.hashgraph.sdk.TransactionId;
import com.hedera.hashgraph.sdk.consensus.SubmitMessageTransaction;
import com.hedera.hashgraph.sdk.proto.Timestamp;
import com.hedera.hcslib.interfaces.LibMessagePersistence;
import com.hedera.hcslib.interfaces.MessagePersistenceLevel;
import com.hedera.hcslib.proto.java.ApplicationMessage;
import com.hedera.hcslib.proto.java.ApplicationMessageChunk;
import com.hedera.hcslib.proto.java.ApplicationMessageId;
import com.hedera.mirror.api.proto.java.MirrorGetTopicMessages;
import com.hedera.mirror.api.proto.java.MirrorGetTopicMessages.MirrorGetTopicMessagesResponse;
import com.hedera.plugin.persistence.config.Config;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PersistMessages 
        implements LibMessagePersistence{
    
    private Map<ApplicationMessageId, List<ApplicationMessageChunk>> partialMessages;
    private Map<TransactionId, SubmitMessageTransaction> transactions;
    private Map<Timestamp, MirrorGetTopicMessagesResponse> mirrorTopicMessages;
    private Map<ApplicationMessageId, ApplicationMessage> applicationMessages;
    
    private Config config = null;
    private MessagePersistenceLevel persistenceLevel = null;
    
    public PersistMessages() throws IOException{
        config = new Config();
        persistenceLevel = config.getConfig().getPersistenceLevel();
        partialMessages = new HashMap<>();
        transactions = new HashMap<>();
        mirrorTopicMessages = new HashMap<>();
        applicationMessages = new HashMap<>();

    }

//    0: none
//    1: timestamp, hash, signature and content for my messages (those I sent or those sent to me)
//    2: 1+ timestamps, hashes and signatures for all messages (regardless of sender/recipient), and content only for my messages
//    3: timestamp, hash, signature and contents for all messages
//  
    // Mirror responses
    @Override
    public void storeMirrorResponse(MirrorGetTopicMessagesResponse mirrorTopicMessageResponse) {
        mirrorTopicMessages.put(mirrorTopicMessageResponse.getConsensusTimestamp(), mirrorTopicMessageResponse);
    }
    
    @Override 
    public MirrorGetTopicMessagesResponse mirrorResponse(Timestamp timestamp) {
        return mirrorTopicMessages.get(timestamp);
    }
        
    @Override 
    public Map<Timestamp, MirrorGetTopicMessagesResponse> mirrorResponses() {
        return mirrorTopicMessages;
    }

    // Transactions
    @Override
    public void storeTransaction(TransactionId transactionId, SubmitMessageTransaction submitMessageTransaction) {
        transactions.put(transactionId, submitMessageTransaction);
    }
    
    @Override 
    public SubmitMessageTransaction submittedTransaction(TransactionId transactionId) {
        return transactions.get(transactionId);
    }

    @Override 
    public Map<TransactionId, SubmitMessageTransaction> submittedTransactions() {
        return transactions;
    }
    
    @Override
    public List<ApplicationMessageChunk> getParts(ApplicationMessageId applicationMessageId) {
        return this.partialMessages.get(applicationMessageId);
    }

    @Override
    public void storeApplicationMessage(ApplicationMessageId applicationMessageId, ApplicationMessage applicationMessage) {
        applicationMessages.put(applicationMessageId, applicationMessage);
    }

    @Override
    public ApplicationMessage getApplicationMessage(ApplicationMessageId applicationMessageId) {
        return applicationMessages.get(applicationMessageId);
    }

    
    @Override
    public Map<ApplicationMessageId, ApplicationMessage> getApplicationMessages() {
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
}
