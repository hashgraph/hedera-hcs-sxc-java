package com.hedera.plugin.persistence.inmemory;

import com.hedera.hcslib.interfaces.LibMessagePersistence;
import com.hedera.hcslib.interfaces.MessagePersistenceLevel;
import com.hedera.hcslib.proto.java.ApplicationMessage;
import com.hedera.hcslib.proto.java.ApplicationMessageChunk;
import com.hedera.hcslib.proto.java.TransactionID;
import com.hedera.plugin.persistence.config.Config;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PersistMessages 
        implements LibMessagePersistence{
    
    

    private Map<TransactionID, List<ApplicationMessageChunk>> partialMessages;
    private Config config = null;
    private MessagePersistenceLevel persistenceLevel = null;
    public PersistMessages() throws IOException{
        config = new Config();
        persistenceLevel = config.getConfig().getPersistenceLevel();
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
        
         switch (persistenceLevel) {
            case FULL:
            case MESSAGE_AND_PARTS:
            case MESSAGE_ONLY:
            case NONE:
            default: this.partialMessages.put(applicationMessageId, l);            
        }
       
    }

    @Override
    public void removeChunks(TransactionID applicationMessageId) {
        switch (persistenceLevel) {
            case FULL:
            case MESSAGE_AND_PARTS:
            case MESSAGE_ONLY:
            case NONE:
            default: this.partialMessages.remove(applicationMessageId);
        
        }
    }
    

}
