package com.hedera.hcslib.interfaces;


import com.hedera.hashgraph.sdk.TransactionId;
import com.hedera.hashgraph.sdk.consensus.ConsensusMessage;
import com.hedera.hashgraph.sdk.consensus.ConsensusMessageSubmitTransaction;
import com.hedera.hcslib.proto.java.ApplicationMessage;
import com.hedera.hcslib.proto.java.ApplicationMessageChunk;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import com.hedera.hcslib.proto.java.ApplicationMessageId;

public interface LibMessagePersistence {
    // message chunking persistence
    public List<ApplicationMessageChunk> getParts(ApplicationMessageId applicationMessageId);
    public void putParts(ApplicationMessageId applicationMessageId, List<ApplicationMessageChunk> l);
    public void removeParts(ApplicationMessageId messageEnvelopeId);
    
    // mirror message persistence
    void storeMirrorResponse(ConsensusMessage mirrorTopicMessageResponse);
    public LibConsensusMessage getMirrorResponse(String timestamp);
    public Map<String, LibConsensusMessage> getMirrorResponses();

    // HCS transaction persistence
    void storeTransaction(TransactionId transactionId, ConsensusMessageSubmitTransaction submitMessageTransaction);
    public ConsensusMessageSubmitTransaction getSubmittedTransaction(String transactionId);
    public Map<String, ConsensusMessageSubmitTransaction> getSubmittedTransactions();
    
    // application message persistence
    public void storeApplicationMessage(ApplicationMessageId applicationMessageId, ApplicationMessage applicationMessage);
    public Map<String, ApplicationMessage> getApplicationMessages();
    public ApplicationMessage getApplicationMessage(String applicationMessageId);
    
    // consensus timestamp
    public Instant getLastConsensusTimestamp();
    
    // persistence level
    public void setPersistenceLevel(MessagePersistenceLevel persistenceLevel);
    
    // clear all data
    public void clear();
}
