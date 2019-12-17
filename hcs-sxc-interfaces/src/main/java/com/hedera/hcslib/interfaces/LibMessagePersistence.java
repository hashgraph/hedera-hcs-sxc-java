package com.hedera.hcslib.interfaces;


import com.hedera.hashgraph.sdk.TransactionId;
import com.hedera.hashgraph.sdk.consensus.SubmitMessageTransaction;
import com.hedera.hashgraph.sdk.proto.Timestamp;
import com.hedera.hcslib.proto.java.ApplicationMessage;
import com.hedera.hcslib.proto.java.ApplicationMessageChunk;

import java.util.List;
import java.util.Map;

import com.hedera.hcslib.proto.java.ApplicationMessageId;
import com.hedera.mirror.api.proto.java.MirrorGetTopicMessages;
import com.hedera.mirror.api.proto.java.MirrorGetTopicMessages.MirrorGetTopicMessagesResponse;

public interface LibMessagePersistence {
    // message chunking persistence
    public List<ApplicationMessageChunk> getParts(ApplicationMessageId applicationMessageId);
    public void putParts(ApplicationMessageId applicationMessageId, List<ApplicationMessageChunk> l);
    public void removeParts(ApplicationMessageId messageEnvelopeId);
    
    // mirror message persistence
    void storeMirrorResponse(MirrorGetTopicMessages.MirrorGetTopicMessagesResponse mirrorTopicMessageResponse);
    public MirrorGetTopicMessagesResponse getMirrorResponse(String timestamp);
    public Map<String, MirrorGetTopicMessagesResponse> getMirrorResponses();

    // HCS transaction persistence
    void storeTransaction(TransactionId transactionId, SubmitMessageTransaction submitMessageTransaction);
    public SubmitMessageTransaction getSubmittedTransaction(String transactionId);
    public Map<String, SubmitMessageTransaction> getSubmittedTransactions();
    
    // application message persistence
    public void storeApplicationMessage(ApplicationMessageId applicationMessageId, ApplicationMessage applicationMessage);
    public Map<String, ApplicationMessage> getApplicationMessages();
    public ApplicationMessage getApplicationMessage(String applicationMessageId);
    
    // clear all data
    public void clear();
  }
