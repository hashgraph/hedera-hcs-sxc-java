package com.hedera.hcs.sxc.consensus;


import com.hedera.hashgraph.sdk.TransactionId;
import com.hedera.hashgraph.sdk.account.AccountId;
import com.hedera.hcs.sxc.consensus.OutboundHCSMessage;
import com.hedera.hcs.sxc.proto.ApplicationMessageChunk;
import java.util.List;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;


public class OutboundHCSMessageTest {
    
   
    public OutboundHCSMessageTest() {
    }
    
    @Test
    public void testSingleChunking() {
        List<ApplicationMessageChunk> chunks = OutboundHCSMessage.chunk(new TransactionId(new AccountId(1234L)),"Single Chunk Message".getBytes());
        assertTrue(chunks.size() == 1);
        
    }
    
    @Test
    public void testMultiChunking() {
        String longString = RandomStringUtils.random(5000, true, true);
        List<ApplicationMessageChunk> chunks = OutboundHCSMessage.chunk(new TransactionId(new AccountId(1234L)),longString.getBytes());
        assertTrue(chunks.size() == 2);
    }
    
}
