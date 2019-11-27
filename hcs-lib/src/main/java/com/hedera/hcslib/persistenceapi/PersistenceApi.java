package com.hedera.hcslib.persistenceapi;

import java.util.List;
import java.util.Map;

import com.hedera.hashgraph.sdk.TransactionId;
import com.hedera.hashgraph.sdk.consensus.SubmitMessageTransaction;
import com.hedera.hashgraph.sdk.proto.Timestamp;
import com.hedera.hcslib.interfaces.LibMessagePersistence;
import com.hedera.hcslib.plugins.Plugins;
import com.hedera.hcslib.proto.java.ApplicationMessage;
import com.hedera.hcslib.proto.java.ApplicationMessageChunk;
import com.hedera.hcslib.proto.java.ApplicationMessageId;
import com.hedera.mirror.api.proto.java.MirrorGetTopicMessages;
import com.hedera.mirror.api.proto.java.MirrorGetTopicMessages.MirrorGetTopicMessagesResponse;

public final class PersistenceApi {
    LibMessagePersistence persistence;
    
    public PersistenceApi() throws Exception {
        Class<?> persistenceClass = Plugins.find("com.hedera.plugin.persistence.*", "com.hedera.hcslib.interfaces.LibMessagePersistence", true);
        this.persistence = (LibMessagePersistence)persistenceClass.newInstance();
    }

    public List<ApplicationMessageChunk> getParts(ApplicationMessageId applicationMessageId) {
        return null;
    }

    public MirrorGetTopicMessagesResponse mirrorResponse(Timestamp timestamp) {
        return null;
    }

    public Map<Timestamp, MirrorGetTopicMessagesResponse> mirrorResponses() {
        return null;
    }

    public SubmitMessageTransaction submittedTransaction(TransactionId transactionId) {
        return null;
    }

    public Map<TransactionId, SubmitMessageTransaction> submittedTransactions() {
        return null;
    }

    public Map<ApplicationMessageId, ApplicationMessage> getApplicationMessages() {
        return null;
    }

    public ApplicationMessage getApplicationMessage(ApplicationMessageId applicationMessageId) {
        return null;
    }
}
