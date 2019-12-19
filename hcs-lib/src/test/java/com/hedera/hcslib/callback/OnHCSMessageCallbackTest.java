package com.hedera.hcslib.callback;
//import com.hedera.plugin.persistence.inmemory.JavaInMemoryPersistenceMoveMeOutOfLib;
import com.google.protobuf.InvalidProtocolBufferException;
import com.hedera.hashgraph.sdk.TransactionId;
import com.hedera.hashgraph.sdk.account.AccountId;
import com.hedera.hcslib.consensus.OutboundHCSMessage;
import com.hedera.hcslib.interfaces.LibMessagePersistence;
import com.hedera.hcslib.interfaces.MessagePersistenceLevel;
import com.hedera.hcslib.proto.java.ApplicationMessage;
import com.hedera.hcslib.proto.java.ApplicationMessageChunk;
import com.hedera.plugin.persistence.inmemory.PersistMessages;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import org.apache.commons.lang3.RandomStringUtils;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class OnHCSMessageCallbackTest {    
    
    public OnHCSMessageCallbackTest() {
    }
    TransactionId txId ;
    LibMessagePersistence persistence;
    
    @BeforeEach
    public void setup() throws IOException{
        
        //Optional<Module> findModule = ModuleLayer.boot().findModule("abcde");
        //findModule.get().getResourceAsStream("xyz");
        txId = new TransactionId(new AccountId(1234L));
        persistence = new PersistMessages(); 
    }
    
    @Test
    public void testSingleChunking() throws InvalidProtocolBufferException {
        byte[] message = "Single Chunk Message".getBytes();
        List<ApplicationMessageChunk> chunks = OutboundHCSMessage.chunk(txId,message);
        assertTrue(chunks.size() == 1);
        Optional<ApplicationMessage> messageOptional
                = OnHCSMessageCallback.pushUntilCompleteMessage(chunks.get(0), persistence);
        assertTrue(messageOptional.isPresent());
        ApplicationMessage applicationMessage = messageOptional.get();
        assertArrayEquals(message,applicationMessage.getBusinessProcessMessage().toByteArray());
        
    }
    
    @Test
    public void testMultiChunking() throws InvalidProtocolBufferException {
        byte[] longString = RandomStringUtils.random(5000, true, true).getBytes();
        List<ApplicationMessageChunk> chunks = OutboundHCSMessage.chunk(txId,longString);
        assertTrue(chunks.size() == 2);
        
        Optional<ApplicationMessage> messageOptional = null;
        for (ApplicationMessageChunk messagePart : chunks){
            messageOptional = OnHCSMessageCallback.pushUntilCompleteMessage(messagePart, persistence);;
        }
        assertTrue(messageOptional.isPresent());
        ApplicationMessage applicationMessage = messageOptional.get();
        assertArrayEquals(longString,applicationMessage.getBusinessProcessMessage().toByteArray());
    }
}