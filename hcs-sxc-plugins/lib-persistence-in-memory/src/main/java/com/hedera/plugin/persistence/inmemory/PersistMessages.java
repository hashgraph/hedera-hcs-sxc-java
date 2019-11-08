package com.hedera.plugin.persistence.inmemory;

import com.hedera.hcslib.interfaces.LibMessagePersistence;
import com.hedera.hcslib.interfaces.MessagePersistenceLevel;
import com.hedera.hcslib.proto.java.MessageEnvelope;
import com.hedera.hcslib.proto.java.MessagePart;
import com.hedera.hcslib.proto.java.TransactionID;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PersistMessages 
        implements LibMessagePersistence{

    private Map<TransactionID, List<MessagePart>> partialMessages;
    
    public PersistMessages(){
        partialMessages  = new HashMap<>();
    }
    
   
    
    @Override
    public void storeMessage(MessagePersistenceLevel level, MessageEnvelope.Builder messagesResponse) {
    
    }

    @Override
    public List<MessagePart> getParts(TransactionID messageEnvelopeId) {
        return this.partialMessages.get(messageEnvelopeId);
    }

    @Override
    public void putParts(TransactionID messageEnvelopeId, List l) {
        this.partialMessages.put(messageEnvelopeId, l);
    }

    @Override
    public void removeParts(TransactionID messageEnvelopeId) {
        this.partialMessages.remove(messageEnvelopeId);
    }
    

}
