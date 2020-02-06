package com.hedera.hcs.sxc.interfaces;

import com.google.protobuf.InvalidProtocolBufferException;
import com.hedera.hashgraph.sdk.TransactionId;
import com.hedera.hashgraph.sdk.consensus.ConsensusMessageSubmitTransaction;
import com.hedera.hcs.sxc.commonobjects.SxcConsensusMessage;
import com.hedera.hcs.sxc.proto.ApplicationMessage;
import com.hedera.hcs.sxc.proto.ApplicationMessageChunk;
import com.hedera.hcs.sxc.proto.ApplicationMessageId;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import javax.crypto.KeyAgreement;


public interface SxcMessagePersistence {
    // Hibernate properties
    public void setHibernateProperties(Map<String, String> hibernateProperties);
    
    // message chunking persistence
    public List<ApplicationMessageChunk> getParts(ApplicationMessageId applicationMessageId);
    public void putParts(ApplicationMessageId applicationMessageId, List<ApplicationMessageChunk> l);
    public void removeParts(ApplicationMessageId messageEnvelopeId);
    
    // mirror message persistence
    void storeMirrorResponse(SxcConsensusMessage mirrorTopicMessageResponse);
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
    
    // secret key  and keySpec holder for key rotation
    public void storeSecretKey(byte[] secretKey);
    public byte[] getSecretKey();
   
    public void storePublicKey(byte[] secretKey);
    public byte[] getPublicKey();
    
    // consensus timestamp
    public Instant getLastConsensusTimestamp();
    
    // persistence level
    public void setPersistenceLevel(MessagePersistenceLevel persistenceLevel);
    
    // clear all data
    public void clear();
  }
