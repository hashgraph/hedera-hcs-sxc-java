package com.hedera.hcs.sxc.interfaces;

import com.google.protobuf.InvalidProtocolBufferException;
import com.hedera.hashgraph.sdk.TransactionId;
import com.hedera.hashgraph.sdk.consensus.ConsensusMessage;
import com.hedera.hashgraph.sdk.consensus.ConsensusMessageSubmitTransaction;
import com.hedera.hcs.sxc.proto.java.ApplicationMessage;
import com.hedera.hcs.sxc.proto.java.ApplicationMessageChunk;
import com.hedera.hcs.sxc.proto.java.ApplicationMessageId;

import java.time.Instant;
import java.util.List;
import java.util.Map;


public interface SxcMessagePersistence {
    // Hibernate properties
    public void setHibernateProperties(Map<String, String> hibernateProperties);
    
    // message chunking persistence
    public List<ApplicationMessageChunk> getParts(ApplicationMessageId applicationMessageId);
    public void putParts(ApplicationMessageId applicationMessageId, List<ApplicationMessageChunk> l);
    public void removeParts(ApplicationMessageId messageEnvelopeId);
    
    // mirror message persistence
    void storeMirrorResponse(ConsensusMessage mirrorTopicMessageResponse);
    public SxcConsensusMessage getMirrorResponse(String timestamp);
    public Map<String, SxcConsensusMessage> getMirrorResponses(String fromTimestamp, String toTimestamp);
    public Map<String, SxcConsensusMessage> getMirrorResponses();

    // HCS transaction persistence
    void storeTransaction(TransactionId transactionId, ConsensusMessageSubmitTransaction submitMessageTransaction);
    public ConsensusMessageSubmitTransaction getSubmittedTransaction(String transactionId);
    public Map<String, ConsensusMessageSubmitTransaction> getSubmittedTransactions();
    
    // application message persistence
    public void storeApplicationMessage(ApplicationMessageId applicationMessageId, ApplicationMessage applicationMessage);
    public Map<String, ApplicationMessage> getApplicationMessages();
    public ApplicationMessage getApplicationMessage(String applicationMessageId) throws InvalidProtocolBufferException;
    
    // consensus timestamp
    public Instant getLastConsensusTimestamp();
    
    // persistence level
    public void setPersistenceLevel(MessagePersistenceLevel persistenceLevel);
    
    // clear all data
    public void clear();
  }
