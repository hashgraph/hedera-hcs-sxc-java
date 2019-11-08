package com.hedera.hcslib.callback;
//import com.hedera.plugin.persistence.inmemory.JavaInMemoryPersistenceMoveMeOutOfLib;
import com.google.protobuf.InvalidProtocolBufferException;
import com.hedera.hashgraph.sdk.TransactionId;
import com.hedera.hashgraph.sdk.account.AccountId;
import com.hedera.hcslib.consensus.OutboundHCSMessage;
import com.hedera.hcslib.interfaces.LibMessagePersistence;
import com.hedera.hcslib.proto.java.MessageEnvelope;
import com.hedera.hcslib.proto.java.MessagePart;
import com.hedera.plugin.persistence.inmemory.PersistMessages;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import org.apache.commons.lang3.RandomStringUtils;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class OnHCSMessageCallbackTest {    
    
    public OnHCSMessageCallbackTest() {
    }
    TransactionId txId ;
    LibMessagePersistence persistence;
    
    @BeforeEach
    public void steup() throws IOException{
        
        //Optional<Module> findModule = ModuleLayer.boot().findModule("abcde");
        //findModule.get().getResourceAsStream("xyz");
        txId = new TransactionId(new AccountId(1234L));
        persistence = new PersistMessages(); // TODO use classloader;
    }
    
    @Test
    public void testSingleChunking() throws InvalidProtocolBufferException {
        String message = "Single Chunk Message";
        List<MessagePart> chunks = OutboundHCSMessage.chunk(txId,message);
        assertTrue(chunks.size() == 1);
        Optional<MessageEnvelope> messageOptional
                = OnHCSMessageCallback.pushUntilCompleteMessage(chunks.get(0), persistence);
        assertTrue(messageOptional.isPresent());
        MessageEnvelope messageEnvelope = messageOptional.get();
        assertTrue(
                message.equals(
                        messageEnvelope.getMessageEnvelope().toStringUtf8()
                )
        );
        
    }
    
    @Test
    public void testMultiChunking() throws InvalidProtocolBufferException {
        String longString = RandomStringUtils.random(5000, true, true);
        List<MessagePart> chunks = OutboundHCSMessage.chunk(txId,longString);
        assertTrue(chunks.size() == 2);
        
        Optional<MessageEnvelope> messageOptional = null;
        for (MessagePart messagePart : chunks){
            messageOptional = OnHCSMessageCallback.pushUntilCompleteMessage(messagePart, persistence);;
        }
        assertTrue(messageOptional.isPresent());
        MessageEnvelope messageEnvelope = messageOptional.get();
        assertTrue(
                longString.equals(
                        messageEnvelope.getMessageEnvelope().toStringUtf8()
                )
        );
        
      
        
    }
}