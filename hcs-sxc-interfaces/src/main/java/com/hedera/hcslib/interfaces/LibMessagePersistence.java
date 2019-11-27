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
    void storeMirrorResponse(MirrorGetTopicMessages.MirrorGetTopicMessagesResponse mirrorTopicMessageResponse);
    void storeTransaction(TransactionId transactionId, SubmitMessageTransaction submitMessageTransaction);
    public List<ApplicationMessageChunk> getParts(ApplicationMessageId applicationMessageId);
    public void putParts(ApplicationMessageId applicationMessageId, List<ApplicationMessageChunk> l);
    public void removeParts(ApplicationMessageId messageEnvelopeId);
    public MirrorGetTopicMessagesResponse mirrorResponse(Timestamp timestamp);
    public Map<Timestamp, MirrorGetTopicMessagesResponse> mirrorResponses();
    public SubmitMessageTransaction submittedTransaction(TransactionId transactionId);
    public Map<TransactionId, SubmitMessageTransaction> submittedTransactions();
    public void storeApplicationMessage(ApplicationMessageId applicationMessageId, ApplicationMessage applicationMessage);
    public Map<ApplicationMessageId, ApplicationMessage> getApplicationMessages();
    public ApplicationMessage getApplicationMessage(ApplicationMessageId applicationMessageId);
}
