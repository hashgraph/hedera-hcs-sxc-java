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
import com.hedera.hashgraph.sdk.TransactionId;
import com.hedera.hashgraph.sdk.account.AccountId;
import com.hedera.hcs.sxc.HCSCore;
import com.hedera.hcs.sxc.consensus.OutboundHCSMessage;
import com.hedera.hcs.sxc.interfaces.HCSResponse;
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
import java.util.Optional;
import org.apache.commons.lang3.RandomStringUtils;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

public class OnHCSMessageCallbackTest {    
    
    @Test
    public void testInstantiation() throws Exception {
         HCSCore hcsCore = HCSCore.INSTANCE
            .singletonInstance("0", "./src/test/resources/config.yaml", "./src/test/resources/dotenv.test");

        OnHCSMessageCallback onHCSMessageCallback = new OnHCSMessageCallback(hcsCore);
        
        hcsCore = HCSCore.INSTANCE
           .singletonInstance("0", "./src/test/resources/config.yaml", "./src/test/resources/dotenv.test");

        onHCSMessageCallback = new OnHCSMessageCallback(hcsCore);
        
        //public void partialMessage(ApplicationMessageChunk messagePart) throws InvalidProtocolBufferException {
    }
    
    @Test
    public void testAddObserverAndNotify() throws Exception {
         HCSCore hcsCore = HCSCore.INSTANCE
                .singletonInstance("0", "./src/test/resources/config.yaml", "./src/test/resources/dotenv.test");
        
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
        List<ApplicationMessageChunk> chunks = OutboundHCSMessage.chunk(new TransactionId(new AccountId(1234L)),message);
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
        byte[] longString = RandomStringUtils.random(5000, true, true).getBytes();
        List<ApplicationMessageChunk> chunks = OutboundHCSMessage.chunk(new TransactionId(new AccountId(1234L)),longString);
        assertTrue(chunks.size() == 2);
        
        Optional<ApplicationMessage> messageOptional = null;
        SxcPersistence persistence = new Persist(); 
        for (ApplicationMessageChunk messagePart : chunks){
            messageOptional = OnHCSMessageCallback.pushUntilCompleteMessage(messagePart, persistence);;
        }
        assertTrue(messageOptional.isPresent());
        ApplicationMessage applicationMessage = messageOptional.get();
        assertArrayEquals(longString,applicationMessage.getBusinessProcessMessage().toByteArray());
    }
}
