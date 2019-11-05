package com.hedera.hcslib.callback;

import com.hedera.hcslib.interfaces.LibMessagePersistence;
import com.hedera.hcslib.interfaces.MessagePersistenceLevel;
import com.hedera.hcslib.proto.java.MessageEnvelope;

public class JavaInMemoryPersistenceMoveMeOutOfLib 
        implements LibMessagePersistence{

    @Override
    public void storeMessage(MessagePersistenceLevel level, MessageEnvelope.Builder messagesResponse) {
    
    }
    

}
