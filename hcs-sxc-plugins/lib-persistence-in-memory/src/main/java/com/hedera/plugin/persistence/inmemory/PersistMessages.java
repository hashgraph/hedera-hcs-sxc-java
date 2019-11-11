package com.hedera.plugin.persistence.inmemory;

import com.hedera.hcslib.interfaces.LibMessagePersistence;
import com.hedera.hcslib.interfaces.MessagePersistenceLevel;
import com.hedera.hcslib.proto.java.ApplicationMessage;
import com.hedera.hcslib.proto.java.ApplicationMessageChunk;
import com.hedera.hcslib.proto.java.TransactionID;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PersistMessages 
        implements LibMessagePersistence{

    private Map<TransactionID, List<ApplicationMessageChunk>> partialMessages;
    
    public PersistMessages(){
        partialMessages  = new HashMap<>();
    }
    
    @Override
    public void storeMessage(MessagePersistenceLevel level, ApplicationMessage.Builder applicationMessage) {
    
    }

    @Override
    public List<ApplicationMessageChunk> getParts(TransactionID applicationMessageId) {
        return this.partialMessages.get(applicationMessageId);
    }

    @Override
    public void putChunks(TransactionID applicationMessageId, List l) {
        this.partialMessages.put(applicationMessageId, l);
    }

    @Override
    public void removeChunks(TransactionID applicationMessageId) {
        this.partialMessages.remove(applicationMessageId);
    }
    

}
