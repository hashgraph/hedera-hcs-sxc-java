package com.hedera.hcslib.callback;

import com.hedera.hcslib.interfaces.LibMessagePersistence;
import com.hedera.hcslib.interfaces.MessagePersistenceLevel;
import com.hedera.hcslib.proto.java.MessageEnvelope;
import com.hedera.hcslib.proto.java.MessagePart;
import com.hedera.hcslib.proto.java.TransactionID;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class JavaInMemoryPersistenceMoveMeOutOfLib 
        implements LibMessagePersistence{

    Map<TransactionID, List<MessagePart>> partialMessages = new HashMap<>();
    
   
    
    @Override
    public void storeMessage(MessagePersistenceLevel level, MessageEnvelope.Builder messagesResponse) {
    
    }

    @Override
    public List<MessagePart> get(TransactionID messageEnvelopeId) {
        return this.partialMessages.get(messageEnvelopeId);
    }

    @Override
    public void put(TransactionID messageEnvelopeId, List l) {
        this.partialMessages.put(messageEnvelopeId, l);
    }

    @Override
    public void remove(TransactionID messageEnvelopeId) {
        this.partialMessages.remove(messageEnvelopeId);
    }
    

}
