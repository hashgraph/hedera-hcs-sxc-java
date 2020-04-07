package com.hedera.hcs.sxc;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import com.hedera.hashgraph.sdk.account.AccountId;
import com.hedera.hashgraph.sdk.crypto.ed25519.Ed25519PrivateKey;
import com.hedera.hcs.sxc.config.Topic;

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


class HCSCoreTest {
    
    @Test
    public void testCore() throws Exception {
        Ed25519PrivateKey msgEncryptKey = Ed25519PrivateKey.generate();
        Ed25519PrivateKey operatorKey = Ed25519PrivateKey.generate();
        
        Map<AccountId, String> nodeMap = Map.of(AccountId.fromString("2.2.2"),"node1", AccountId.fromString("3.3.3"),"node2");
        Topic topic = new Topic();
        List<Topic> topics = new ArrayList<Topic>();
        topics.add(topic);
        
        HCSCore hcsCore = new HCSCore().builder("0", "./src/test/resources/config.yaml", "./src/test/resources/dotenv.test")
                .withMessageEncryptionKey(msgEncryptKey.toBytes())
                .withEncryptedMessages(true)
                .withKeyRotation(true, 5)
                .withMessageSignature(true)
                .withNodeMap(nodeMap)
                .withOperatorAccountId(AccountId.fromString("1.1.1"))
                .withOperatorKey(operatorKey)
                .withTopicList(topics);
        
        assertEquals("0", hcsCore.getApplicationId());
        assertTrue(hcsCore.getCatchupHistory());
        assertEquals(1, hcsCore.getConsensusTopicIds().size());
        assertEquals(1, hcsCore.getTopics().size());
        assertArrayEquals(operatorKey.toBytes(), hcsCore.getOperatorKey().toBytes());
        assertTrue(hcsCore.getEncryptMessages());
        assertEquals(1, hcsCore.getHibernateConfig().size());
        assertEquals(100000000, hcsCore.getMaxTransactionFee());
        assertArrayEquals(msgEncryptKey.toBytes(), hcsCore.getMessageEncryptionKey());
        assertTrue(hcsCore.getPersistence() != null);
        assertEquals("hcs.testnet.mirrornode.hedera.com:5600", hcsCore.getMirrorAddress());
        assertEquals(2, hcsCore.getNodeMap().size());
        assertEquals(AccountId.fromString("1.1.1"), hcsCore.getOperatorAccountId());
        assertTrue(hcsCore.getRotateKeys());
        assertEquals(5, hcsCore.getRotationFrequency());
        assertTrue(hcsCore.getSignMessages());
        assertNull(hcsCore.getTempKeyAgreement());
    }
    
    @Test
    public void testCoreEmptyEnvironment() throws Exception {
        @SuppressWarnings("unused")
        HCSCore hcsCore = new HCSCore().builder("0", "./src/test/resources/config.yaml", "./src/test/resources/dotenvempty.test");
    }
    
    @Test
    public void testSystemProperties() throws Exception {
        System.setProperty("OPERATOR_KEY", "302e020100300506032b657004220420abb9499694bad1f081cb2a55a08989303cbc3322fae657db1044fdbf3b9eed65");
        System.setProperty("OPERATOR_ID", "0.0.2");
        HCSCore hcsCore = new HCSCore().builder("0", "./src/test/resources/config.yaml", "./src/test/resources/dotenvempty.test");
        
        assertEquals("302e020100300506032b657004220420abb9499694bad1f081cb2a55a08989303cbc3322fae657db1044fdbf3b9eed65", hcsCore.getOperatorKey().toString());
        assertEquals("0.0.2", hcsCore.getOperatorAccountId().toString());
    }

    @Test
    public void testCoreErrorConditions() {
        assertThrows(Exception.class, () -> {coreEncryptNoKey();});
        assertThrows(Exception.class, () -> {coreRotateNoEncryption();});
        assertThrows(Exception.class, () -> {coreRotateZero();});
    }
    
    private void coreEncryptNoKey() throws Exception {
        @SuppressWarnings("unused")
        HCSCore hcsCore = new HCSCore().builder("0", "./src/test/resources/config.yaml", "./src/test/resources/dotenv.test")
            .withEncryptedMessages(true);
    }
    private void coreRotateNoEncryption() throws Exception {
        @SuppressWarnings("unused")
        HCSCore hcsCore = new HCSCore().builder("0", "./src/test/resources/config.yaml", "./src/test/resources/dotenv.test")
            .withEncryptedMessages(false)
            .withKeyRotation(true, 4);
    }
    private void coreRotateZero() throws Exception {
        @SuppressWarnings("unused")
        HCSCore hcsCore = new HCSCore().builder("0", "./src/test/resources/config.yaml", "./src/test/resources/dotenv.test")
            .withMessageEncryptionKey("key".getBytes())
            .withEncryptedMessages(true)
            .withKeyRotation(true, 0);
    }
}
