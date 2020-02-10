package com.hedera.hcs.sxc.consensus;

/*-
 * ‌
 * hcs-sxc-java
 * ​
 * Copyright (C) 2019 - 2020 Hedera Hashgraph, LLC
 * ​
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ‍
 */


import com.hedera.hashgraph.sdk.TransactionId;
import com.hedera.hashgraph.sdk.account.AccountId;
import com.hedera.hashgraph.sdk.crypto.ed25519.Ed25519PrivateKey;
import com.hedera.hcs.sxc.HCSCore;
import com.hedera.hcs.sxc.consensus.OutboundHCSMessage;
import com.hedera.hcs.sxc.proto.ApplicationMessageChunk;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;


public class OutboundHCSMessageTest {
    
   
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
    
    @Test
    public void testOutBoundMessageBuilder() throws Exception {
         HCSCore hcsCore = HCSCore.INSTANCE
                .singletonInstanceWithAppIdEnvAndConfig
                 (0, "./src/test/resources/config.yaml", "./src/test/resources/dotenv.test");

        Ed25519PrivateKey ed25519PrivateKey = Ed25519PrivateKey.generate();
        TransactionId transactionId = TransactionId.withValidStart(AccountId.fromString("0.0.10"), Instant.now());
        
        OutboundHCSMessage outboundHCSMessage = new OutboundHCSMessage(hcsCore);
        // test default values
        assertFalse(outboundHCSMessage.getOverrideEncryptedMessages());
        assertFalse(outboundHCSMessage.getOverrideKeyRotation());
        assertEquals(0, outboundHCSMessage.getOverrideKeyRotationFrequency());
        assertFalse(outboundHCSMessage.getOverrideMessageSignature());
        assertEquals("0.0.2", outboundHCSMessage.getOverrideOperatorAccountId().toString());
        assertEquals("302e020100300506032b657004220420abb9499694bad1f081cb2a55a08989303cbc3322fae657db1044fdbf3b9eed65", outboundHCSMessage.getOverrideOperatorKey().toString());
        assertNull(outboundHCSMessage.getFirstTransactionId());
        // override defaults
        outboundHCSMessage.overrideEncryptedMessages(true)
            .overrideKeyRotation(true, 5)
            .overrideMessageSignature(true)
            .overrideNodeMap(Map.of(AccountId.fromString("0.0.19"),"testing.hedera.com"))
            .overrideOperatorAccountId(AccountId.fromString("0.0.5"))
            .overrideOperatorKey(ed25519PrivateKey)
            .withFirstTransactionId(transactionId);
        // test updated values
        assertTrue(outboundHCSMessage.getOverrideEncryptedMessages());
        assertTrue(outboundHCSMessage.getOverrideKeyRotation());
        assertTrue(outboundHCSMessage.getOverrideMessageSignature());
        assertEquals(5, outboundHCSMessage.getOverrideKeyRotationFrequency());
        assertEquals(1, outboundHCSMessage.getOverrideNodeMap().size());
        assertEquals("0.0.5", outboundHCSMessage.getOverrideOperatorAccountId().toString());
        assertEquals("testing.hedera.com", outboundHCSMessage.getOverrideNodeMap().get(AccountId.fromString("0.0.19")));
        assertArrayEquals(ed25519PrivateKey.toBytes(), outboundHCSMessage.getOverrideOperatorKey().toBytes());
        assertEquals(transactionId.accountId.toString(), outboundHCSMessage.getFirstTransactionId().accountId.toString());
        assertEquals(transactionId.validStart, outboundHCSMessage.getFirstTransactionId().validStart);
    }
    
}
