package com.hedera.hcs.sxc.callback;

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
import com.google.protobuf.ByteString;
import com.hedera.hashgraph.proto.mirror.ConsensusTopicResponse;
import com.hedera.hashgraph.sdk.TransactionId;
import com.hedera.hashgraph.sdk.account.AccountId;
import com.hedera.hashgraph.sdk.consensus.ConsensusTopicId;
import com.hedera.hashgraph.sdk.crypto.ed25519.Ed25519PrivateKey;
import com.hedera.hcs.sxc.HCSCore;
import com.hedera.hcs.sxc.commonobjects.HCSResponse;
import com.hedera.hcs.sxc.commonobjects.SxcConsensusMessage;
import com.hedera.hcs.sxc.consensus.OutboundHCSMessage;
import com.hedera.hcs.sxc.interfaces.SxcPersistence;
import com.hedera.hcs.sxc.plugin.persistence.inmemory.Persist;
import com.hedera.hcs.sxc.proto.AccountID;
import com.hedera.hcs.sxc.proto.ApplicationMessage;
import com.hedera.hcs.sxc.proto.ApplicationMessageChunk;
import com.hedera.hcs.sxc.proto.ApplicationMessageID;
import com.hedera.hcs.sxc.proto.Timestamp;


import java.io.IOException;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.apache.commons.lang3.RandomStringUtils;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import org.junit.jupiter.api.Test;

public class OnHCSMessageCallbackTest {    
    
    @Test
    public void testInstantiation() {
        try {
            HCSCore hcsCore = new HCSCore().builder("0", "./src/test/resources/config.yaml", "./src/test/resources/dotenv.test");
            
            @SuppressWarnings("unused")
                    OnHCSMessageCallback onHCSMessageCallback = new OnHCSMessageCallback(hcsCore);
        } catch (Exception ex) {
            fail();
        }
        
    }
    
    @Test
    public void testAddObserverAndNotify() throws Exception {
        HCSCore hcsCore = new HCSCore().builder("0", "./src/test/resources/config.yaml", "./src/test/resources/dotenv.test");
        
        OnHCSMessageCallback onHCSMessageCallback = new OnHCSMessageCallback(hcsCore);
        onHCSMessageCallback.addObserver(hcsMessage -> {
            processHCSMessage(hcsMessage);
        });
        ApplicationMessageID applicationMessageID = ApplicationMessageID.newBuilder()
                .setAccountID(AccountID.newBuilder()
                        .setShardNum(0)
                        .setRealmNum(0)
                        .setAccountNum(1)
                        .build()
                )
                .setValidStart(Timestamp.newBuilder()
                        .setSeconds(Instant.now().getEpochSecond())
                        .setNanos(Instant.now().getNano())
                        .build()
                ).build();        
        
        onHCSMessageCallback.notifyObservers("notification".getBytes(), applicationMessageID);
    }
    
    public void processHCSMessage(HCSResponse hcsResponse) {
    }
    
    @Test
    public void testSingleChunking() throws IOException {
        byte[] message = "Single Chunk Message".getBytes();
        HCSCore hcsCore  = null;
        try {
            hcsCore = new HCSCore().builder("0", "./src/test/resources/config.yaml", "./src/test/resources/dotenv.test");
        } catch (Exception ex) {
            fail("Core failed to init");
        }

        
        List<ApplicationMessageChunk> chunks = OutboundHCSMessage.chunk(new TransactionId(new AccountId(1234L)),hcsCore,message,null);
        assertTrue(chunks.size() == 1);
        SxcPersistence persistence = new Persist(); 
        Optional<ApplicationMessage> messageOptional
                = OnHCSMessageCallback.pushUntilCompleteMessage(chunks.get(0), persistence);
        assertTrue(messageOptional.isPresent());
        ApplicationMessage applicationMessage = messageOptional.get();
        assertArrayEquals(message,applicationMessage.getBusinessProcessMessage().toByteArray());
        
    }
    
    @Test
    public void testMultiChunking() throws IOException {
        byte[] message = "Single Chunk Message".getBytes();
        HCSCore hcsCore  = null;
        try {
            hcsCore = new HCSCore().builder("0", "./src/test/resources/config.yaml", "./src/test/resources/dotenv.test");
        } catch (Exception ex) {
            fail("Core failed to init");
        }

        
        
        byte[] longString = RandomStringUtils.random(5000, true, true).getBytes();
        List<ApplicationMessageChunk> chunks = OutboundHCSMessage.chunk(new TransactionId(new AccountId(1234L)),hcsCore,longString,null);
        assertTrue(chunks.size() == 2);
        
        Optional<ApplicationMessage> messageOptional = null;
        SxcPersistence persistence = new Persist(); 
        for (ApplicationMessageChunk messagePart : chunks){
            messageOptional = OnHCSMessageCallback.pushUntilCompleteMessage(messagePart, persistence);
        }
        assertTrue(messageOptional.isPresent());
        ApplicationMessage applicationMessage = messageOptional.get();
        assertArrayEquals(longString,applicationMessage.getBusinessProcessMessage().toByteArray());
    }
    
    
    @Test
    public void testSingleChunkUnencrypted() throws IOException, Exception {
        byte[] message = "Single Chunk Message".getBytes();
        HCSCore hcsCore  = null;

        hcsCore = new HCSCore().builder("0", "./src/test/resources/config.yaml", "./src/test/resources/dotenv.test")
          .withMessageSigningKey(Ed25519PrivateKey.generate())
        ;
        hcsCore.addOrUpdateAppParticipant("1", 
                "302a300506032b6570032100c969fbb7b67b36f5560aa59a754a38bd88fd53ff870dad33011bbe2f37f34396", 
                "817c2d3fc1188a7007bce96d5760dd06d3635f378322c98085b4bb37d63c2449"
        );

       
        
        List<ApplicationMessageChunk> chunks = OutboundHCSMessage.chunk(new TransactionId(new AccountId(1234L)),hcsCore,message, Map.of("sharedSymmetricEncryptionKey","817c2d3fc1188a7007bce96d5760dd06d3635f378322c98085b4bb37d63c2449")  );
        assertTrue(chunks.size() == 1);
        
        ConsensusTopicId consensusTopicId = new ConsensusTopicId(1, 2, 3);
        com.hedera.hashgraph.proto.Timestamp timestamp2 = com.hedera.hashgraph.proto.Timestamp.newBuilder()
                        .setSeconds(Instant.now().getEpochSecond())
                        .setNanos(Instant.now().getNano())
                        .build();
        ConsensusTopicResponse consensusTopicResponse = ConsensusTopicResponse
                .newBuilder()
                .setConsensusTimestamp(timestamp2)
                .setMessage(ByteString.copyFromUtf8("message"))
                .setRunningHash(ByteString.copyFromUtf8("runninghash"))
                .setSequenceNumber(20)
                .build();
        
        SxcConsensusMessage sxcConsensusMessage = new SxcConsensusMessage(consensusTopicId, consensusTopicResponse);
        OnHCSMessageCallback cb = new OnHCSMessageCallback(hcsCore);
        assertDoesNotThrow( () -> {
            cb.partialMessage(chunks.get(0), sxcConsensusMessage);
        });
        
    }
    
    @Test
    public void testSingleChunkEncryptedSentByMe() throws IOException, Exception {
        byte[] message = "Single Chunk Message".getBytes();
        HCSCore hcsCore  = null;

        hcsCore = new HCSCore().builder("0", "./src/test/resources/config3.yaml", "./src/test/resources/dotenv.test")
          .withMessageSigningKey(Ed25519PrivateKey.generate())
        ;
        hcsCore.addOrUpdateAppParticipant("1", 
                "302a300506032b6570032100c969fbb7b67b36f5560aa59a754a38bd88fd53ff870dad33011bbe2f37f34396", 
                "817c2d3fc1188a7007bce96d5760dd06d3635f378322c98085b4bb37d63c2449"
        );

       
        
        List<ApplicationMessageChunk> chunks = OutboundHCSMessage.chunk(new TransactionId(new AccountId(1234L)),hcsCore,message, Map.of("sharedSymmetricEncryptionKey","817c2d3fc1188a7007bce96d5760dd06d3635f378322c98085b4bb37d63c2449")  );
        assertTrue(chunks.size() == 1);
        
        ConsensusTopicId consensusTopicId = new ConsensusTopicId(1, 2, 3);
        com.hedera.hashgraph.proto.Timestamp timestamp2 = com.hedera.hashgraph.proto.Timestamp.newBuilder()
                        .setSeconds(Instant.now().getEpochSecond())
                        .setNanos(Instant.now().getNano())
                        .build();
        ConsensusTopicResponse consensusTopicResponse = ConsensusTopicResponse
                .newBuilder()
                .setConsensusTimestamp(timestamp2)
                .setMessage(ByteString.copyFromUtf8("message"))
                .setRunningHash(ByteString.copyFromUtf8("runninghash"))
                .setSequenceNumber(20)
                .build();
        
        SxcConsensusMessage sxcConsensusMessage = new SxcConsensusMessage(consensusTopicId, consensusTopicResponse);
        OnHCSMessageCallback cb = new OnHCSMessageCallback(hcsCore);
        assertDoesNotThrow( () -> {
            cb.partialMessage(chunks.get(0), sxcConsensusMessage);
        });
        
    }
    
    
    @Test
    public void testSingleChunkEncryptedSentByOtherIsNotForMe() throws IOException, Exception {
        byte[] message = "Single Chunk Message".getBytes();
        HCSCore hcsCore  = null;

        hcsCore = new HCSCore().builder("0", "./src/test/resources/config3.yaml", "./src/test/resources/dotenv.test")
          .withMessageSigningKey(Ed25519PrivateKey.generate())
        ;
        hcsCore.addOrUpdateAppParticipant("1", 
                "302a300506032b6570032100c969fbb7b67b36f5560aa59a754a38bd88fd53ff870dad33011bbe2f37f34396", 
                "123c2d3fc1188a7007bce96d5760dd06d3635f378322c98085b4bb37d63c2449"
        );

       
        
        List<ApplicationMessageChunk> chunks = OutboundHCSMessage.chunk(new TransactionId(new AccountId(1234L)),hcsCore,message, Map.of("sharedSymmetricEncryptionKey","817c2d3fc1188a7007bce96d5760dd06d3635f378322c98085b4bb37d63c2449")  );
        assertTrue(chunks.size() == 1);
        
        ConsensusTopicId consensusTopicId = new ConsensusTopicId(1, 2, 3);
        com.hedera.hashgraph.proto.Timestamp timestamp2 = com.hedera.hashgraph.proto.Timestamp.newBuilder()
                        .setSeconds(Instant.now().getEpochSecond())
                        .setNanos(Instant.now().getNano())
                        .build();
        ConsensusTopicResponse consensusTopicResponse = ConsensusTopicResponse
                .newBuilder()
                .setConsensusTimestamp(timestamp2)
                .setMessage(ByteString.copyFromUtf8("message"))
                .setRunningHash(ByteString.copyFromUtf8("runninghash"))
                .setSequenceNumber(20)
                .build();
        
        SxcConsensusMessage sxcConsensusMessage = new SxcConsensusMessage(consensusTopicId, consensusTopicResponse);
        
      
        OnHCSMessageCallback cb = new OnHCSMessageCallback(hcsCore);
                       
        
        String applicationMessageId = 
                                "0"
                        + "." + "0"
                        + "." + "1234"
                        + "-" + chunks.get(0).getApplicationMessageId().getValidStart().getSeconds()
                        + "-" + chunks.get(0).getApplicationMessageId().getValidStart().getNanos();
        
        // remove from database to simulate message not sent by me
        hcsCore.getPersistence().clear();
        assertDoesNotThrow( () -> {
            cb.partialMessage(chunks.get(0), sxcConsensusMessage);
        });
        
    }
    
    @Test
    public void testSingleChunkEncryptedSentByOtherIsFromFriendButCantDecrypt() throws IOException, Exception {
        byte[] message = "Single Chunk Message".getBytes();
        HCSCore hcsCore  = null;

        hcsCore = new HCSCore().builder("0", "./src/test/resources/config3.yaml", "./src/test/resources/dotenv.test")
          .withMessageSigningKey(Ed25519PrivateKey.fromString("302e020100300506032b657004220420e1c09b75f52f7ba287fea66f2c4dcd316f9f1cba47020e760f3cc940ba516641"))
        ;
        hcsCore.addOrUpdateAppParticipant("1", 
                "302a300506032b6570032100c969fbb7b67b36f5560aa59a754a38bd88fd53ff870dad33011bbe2f37f34396", 
                "123c2d3fc1188a7007bce96d5760dd06d3635f378322c98085b4bb37d63c2449"
        );

       
        
        List<ApplicationMessageChunk> chunks = OutboundHCSMessage.chunk(new TransactionId(new AccountId(1234L)),hcsCore,message, Map.of("sharedSymmetricEncryptionKey","817c2d3fc1188a7007bce96d5760dd06d3635f378322c98085b4bb37d63c2449")  );
        assertTrue(chunks.size() == 1);
        
        ConsensusTopicId consensusTopicId = new ConsensusTopicId(1, 2, 3);
        com.hedera.hashgraph.proto.Timestamp timestamp2 = com.hedera.hashgraph.proto.Timestamp.newBuilder()
                        .setSeconds(Instant.now().getEpochSecond())
                        .setNanos(Instant.now().getNano())
                        .build();
        ConsensusTopicResponse consensusTopicResponse = ConsensusTopicResponse
                .newBuilder()
                .setConsensusTimestamp(timestamp2)
                .setMessage(ByteString.copyFromUtf8("message"))
                .setRunningHash(ByteString.copyFromUtf8("runninghash"))
                .setSequenceNumber(20)
                .build();
        
        SxcConsensusMessage sxcConsensusMessage = new SxcConsensusMessage(consensusTopicId, consensusTopicResponse);
        
      
        OnHCSMessageCallback cb = new OnHCSMessageCallback(hcsCore);
                       
        
        String applicationMessageId = 
                                "0"
                        + "." + "0"
                        + "." + "1234"
                        + "-" + chunks.get(0).getApplicationMessageId().getValidStart().getSeconds()
                        + "-" + chunks.get(0).getApplicationMessageId().getValidStart().getNanos();
        
        // remove from database to simulate message not sent by me
        hcsCore.getPersistence().clear();
        assertDoesNotThrow( () -> {
            cb.partialMessage(chunks.get(0), sxcConsensusMessage);
        });
        
    }
    
    
    
    @Test
    public void testSingleChunkEncryptedSentByOtherIsForMe() throws IOException, Exception {
        byte[] message = "Single Chunk Message".getBytes();
        HCSCore hcsCore  = null;

        hcsCore = new HCSCore().builder("0", "./src/test/resources/config3.yaml", "./src/test/resources/dotenv.test")
          .withMessageSigningKey(Ed25519PrivateKey.fromString("302e020100300506032b657004220420e1c09b75f52f7ba287fea66f2c4dcd316f9f1cba47020e760f3cc940ba516641"))
        ;
        hcsCore.addOrUpdateAppParticipant("1", 
                "302a300506032b6570032100c969fbb7b67b36f5560aa59a754a38bd88fd53ff870dad33011bbe2f37f34396", 
                "123c2d3fc1188a7007bce96d5760dd06d3635f378322c98085b4bb37d63c2449"
        );

       
        
        List<ApplicationMessageChunk> chunks = OutboundHCSMessage.chunk(new TransactionId(new AccountId(1234L)),hcsCore,message, Map.of("sharedSymmetricEncryptionKey","123c2d3fc1188a7007bce96d5760dd06d3635f378322c98085b4bb37d63c2449")  );
        assertTrue(chunks.size() == 1);
        
        ConsensusTopicId consensusTopicId = new ConsensusTopicId(1, 2, 3);
        com.hedera.hashgraph.proto.Timestamp timestamp2 = com.hedera.hashgraph.proto.Timestamp.newBuilder()
                        .setSeconds(Instant.now().getEpochSecond())
                        .setNanos(Instant.now().getNano())
                        .build();
        ConsensusTopicResponse consensusTopicResponse = ConsensusTopicResponse
                .newBuilder()
                .setConsensusTimestamp(timestamp2)
                .setMessage(ByteString.copyFromUtf8("message"))
                .setRunningHash(ByteString.copyFromUtf8("runninghash"))
                .setSequenceNumber(20)
                .build();
        
        SxcConsensusMessage sxcConsensusMessage = new SxcConsensusMessage(consensusTopicId, consensusTopicResponse);
        
      
        OnHCSMessageCallback cb = new OnHCSMessageCallback(hcsCore);
                       
        
        String applicationMessageId = 
                                "0"
                        + "." + "0"
                        + "." + "1234"
                        + "-" + chunks.get(0).getApplicationMessageId().getValidStart().getSeconds()
                        + "-" + chunks.get(0).getApplicationMessageId().getValidStart().getNanos();
        
        // remove from database to simulate message not sent by me
        hcsCore.getPersistence().clear();
        assertDoesNotThrow( () -> {
            cb.partialMessage(chunks.get(0), sxcConsensusMessage);
        });
        
    }
}
