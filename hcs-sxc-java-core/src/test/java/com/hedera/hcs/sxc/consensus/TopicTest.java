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


import com.hedera.hashgraph.sdk.account.AccountId;
import com.hedera.hashgraph.sdk.consensus.ConsensusTopicId;
import com.hedera.hashgraph.sdk.crypto.ed25519.Ed25519PrivateKey;
import com.hedera.hcs.sxc.HCSCore;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class TopicTest {
   
    @Test
    public void testCreateTopic() throws Exception {
        Ed25519PrivateKey ed25519PrivateKey = Ed25519PrivateKey.generate();
        Ed25519PrivateKey adminKey = Ed25519PrivateKey.generate();
        Ed25519PrivateKey submitKey = Ed25519PrivateKey.generate();
        HCSCore hcsCore = new HCSCore().builder("0", "./src/test/resources/config.yaml", "./src/test/resources/dotenv.test")
                .withOperatorAccountId(AccountId.fromString("0.0.2"))
                .withOperatorKey(Ed25519PrivateKey.fromString("302e020100300506032b657004220420abb9499694bad1f081cb2a55a08989303cbc3322fae657db1044fdbf3b9eed65"));

        CreateHCSTopic createHCSTopic = new CreateHCSTopic(hcsCore);

        assertEquals(4, createHCSTopic.getNodeMap().size());
        assertEquals("0.testnet.hedera.com:50211", createHCSTopic.getNodeMap().get(AccountId.fromString("0.0.3")));
        assertEquals("1.testnet.hedera.com:50211", createHCSTopic.getNodeMap().get(AccountId.fromString("0.0.4")));
        assertEquals("2.testnet.hedera.com:50211", createHCSTopic.getNodeMap().get(AccountId.fromString("0.0.5")));
        assertEquals("3.testnet.hedera.com:50211", createHCSTopic.getNodeMap().get(AccountId.fromString("0.0.6")));
        assertEquals("0.0.2", createHCSTopic.getOperatorAccountId().toString());
        assertEquals("302e020100300506032b657004220420abb9499694bad1f081cb2a55a08989303cbc3322fae657db1044fdbf3b9eed65", createHCSTopic.getOperatorKey().toString());

        // override defaults
        createHCSTopic
            .withNodeMap(Map.of(AccountId.fromString("0.0.19"),"testing.hedera.com"))
            .withOperatorAccountId(AccountId.fromString("0.0.10"))
            .withOperatorKey(ed25519PrivateKey)
            .setAdminKey(adminKey.publicKey)
            .setAutoRenewAccountId(AccountId.fromString("0.0.11"))
            .setAutoRenewAccountId("0.0.55")
            .setDuration(Duration.ofMillis(1000))
            .setMaxTransactionFee(100L)
            .setSubmitKey(submitKey.publicKey)
            .setTopicMemo("Topic Memo for test")
            .setTransactionMemo("Transaction Memo for test");
        
        // test updated values
        // note some values are allocated to a Transaction object inside createHCSTopic which don't have getters
        assertEquals(1, createHCSTopic.getNodeMap().size());
        assertEquals("testing.hedera.com", createHCSTopic.getNodeMap().get(AccountId.fromString("0.0.19")));
        assertEquals("0.0.10", createHCSTopic.getOperatorAccountId().toString());
        assertEquals(ed25519PrivateKey.toString(), createHCSTopic.getOperatorKey().toString());
    }

    @Test
    public void testUpdateTopic() throws Exception {
        Ed25519PrivateKey ed25519PrivateKey = Ed25519PrivateKey.generate();
        Ed25519PrivateKey adminKey = Ed25519PrivateKey.generate();
        Ed25519PrivateKey submitKey = Ed25519PrivateKey.generate();
        HCSCore hcsCore = new HCSCore().builder("0", "./src/test/resources/config.yaml", "./src/test/resources/dotenv.test")
                .withOperatorAccountId(AccountId.fromString("0.0.2"))
                .withOperatorKey(Ed25519PrivateKey.fromString("302e020100300506032b657004220420abb9499694bad1f081cb2a55a08989303cbc3322fae657db1044fdbf3b9eed65"));

        UpdateHCSTopic updateHCSTopic = new UpdateHCSTopic(hcsCore);

        assertEquals(4, updateHCSTopic.getNodeMap().size());
        assertEquals("0.testnet.hedera.com:50211", updateHCSTopic.getNodeMap().get(AccountId.fromString("0.0.3")));
        assertEquals("1.testnet.hedera.com:50211", updateHCSTopic.getNodeMap().get(AccountId.fromString("0.0.4")));
        assertEquals("2.testnet.hedera.com:50211", updateHCSTopic.getNodeMap().get(AccountId.fromString("0.0.5")));
        assertEquals("3.testnet.hedera.com:50211", updateHCSTopic.getNodeMap().get(AccountId.fromString("0.0.6")));
        assertEquals("0.0.2", updateHCSTopic.getOperatorAccountId().toString());
        assertEquals("302e020100300506032b657004220420abb9499694bad1f081cb2a55a08989303cbc3322fae657db1044fdbf3b9eed65", updateHCSTopic.getOperatorKey().toString());

        // override defaults
        updateHCSTopic
            .withNodeMap(Map.of(AccountId.fromString("0.0.19"),"testing.hedera.com"))
            .withOperatorAccountId(AccountId.fromString("0.0.10"))
            .withOperatorKey(ed25519PrivateKey)
            .setAdminKey(adminKey.publicKey)
            .setAutoRenewAccountId(AccountId.fromString("0.0.11"))
            .setAutoRenewPeriod(Duration.ofMillis(1000))
            .setAutoRenewAccountId(AccountId.fromString("0.0.22"))
            .setAutoRenewAccountId("0.0.55")
            .setMaxTransactionFee(100L)
            .setExpirationTime(Instant.now())
            .setTopicId(ConsensusTopicId.fromString("0.0.10"))
            .setTopicId("0.0.44")
            .clearAdminKey()
            .clearAutoRenewAccountId()
            .clearSubmitKey()
            .clearTopicMemo()
            .setSubmitKey(submitKey.publicKey)
            .setTopicMemo("Topic Memo for test")
            .setTransactionMemo("Transaction Memo for test");
        
        // test updated values
        // note some values are allocated to a Transaction object inside createHCSTopic which don't have getters
        assertEquals(1, updateHCSTopic.getNodeMap().size());
        assertEquals("testing.hedera.com", updateHCSTopic.getNodeMap().get(AccountId.fromString("0.0.19")));
        assertEquals("0.0.10", updateHCSTopic.getOperatorAccountId().toString());
        assertEquals(ed25519PrivateKey.toString(), updateHCSTopic.getOperatorKey().toString());
    }

    @Test
    public void testDeleteTopic() throws Exception {
        Ed25519PrivateKey ed25519PrivateKey = Ed25519PrivateKey.generate();
//         HCSCore hcsCore = HCSCore.INSTANCE
        HCSCore hcsCore = new HCSCore().builder("0", "./src/test/resources/config.yaml", "./src/test/resources/dotenv.test")
                .withOperatorAccountId(AccountId.fromString("0.0.2"))
                .withOperatorKey(Ed25519PrivateKey.fromString("302e020100300506032b657004220420abb9499694bad1f081cb2a55a08989303cbc3322fae657db1044fdbf3b9eed65"));

        DeleteHCSTopic deleteHCSTopic = new DeleteHCSTopic(hcsCore);

        assertEquals(4, deleteHCSTopic.getNodeMap().size());
        assertEquals("0.testnet.hedera.com:50211", deleteHCSTopic.getNodeMap().get(AccountId.fromString("0.0.3")));
        assertEquals("1.testnet.hedera.com:50211", deleteHCSTopic.getNodeMap().get(AccountId.fromString("0.0.4")));
        assertEquals("2.testnet.hedera.com:50211", deleteHCSTopic.getNodeMap().get(AccountId.fromString("0.0.5")));
        assertEquals("3.testnet.hedera.com:50211", deleteHCSTopic.getNodeMap().get(AccountId.fromString("0.0.6")));
        assertEquals("0.0.2", deleteHCSTopic.getOperatorAccountId().toString());
        assertEquals("302e020100300506032b657004220420abb9499694bad1f081cb2a55a08989303cbc3322fae657db1044fdbf3b9eed65", deleteHCSTopic.getOperatorKey().toString());

        // override defaults
        deleteHCSTopic
            .withNodeMap(Map.of(AccountId.fromString("0.0.19"),"testing.hedera.com"))
            .withOperatorAccountId(AccountId.fromString("0.0.10"))
            .withOperatorKey(ed25519PrivateKey)
            .setMaxTransactionFee(100L)
            .setTopicId(ConsensusTopicId.fromString("0.0.44"))
            .setTopicId("0.0.55")
            .setTransactionMemo("Transaction Memo for test");
        
        // test updated values
        // note some values are allocated to a Transaction object inside createHCSTopic which don't have getters
        assertEquals(1, deleteHCSTopic.getNodeMap().size());
        assertEquals("testing.hedera.com", deleteHCSTopic.getNodeMap().get(AccountId.fromString("0.0.19")));
        assertEquals("0.0.10", deleteHCSTopic.getOperatorAccountId().toString());
        assertEquals(ed25519PrivateKey.toString(), deleteHCSTopic.getOperatorKey().toString());
    }

    @Test
    public void testGetInfoTopic() throws Exception {
        Ed25519PrivateKey ed25519PrivateKey = Ed25519PrivateKey.generate();
        HCSCore hcsCore = new HCSCore().builder("0", "./src/test/resources/config.yaml", "./src/test/resources/dotenv.test")
                .withOperatorAccountId(AccountId.fromString("0.0.2"))
                .withOperatorKey(Ed25519PrivateKey.fromString("302e020100300506032b657004220420abb9499694bad1f081cb2a55a08989303cbc3322fae657db1044fdbf3b9eed65"));

        GetTopicInfo getTopicInfo = new GetTopicInfo(hcsCore);

        assertEquals(4, getTopicInfo.getNodeMap().size());
        assertEquals("0.testnet.hedera.com:50211", getTopicInfo.getNodeMap().get(AccountId.fromString("0.0.3")));
        assertEquals("1.testnet.hedera.com:50211", getTopicInfo.getNodeMap().get(AccountId.fromString("0.0.4")));
        assertEquals("2.testnet.hedera.com:50211", getTopicInfo.getNodeMap().get(AccountId.fromString("0.0.5")));
        assertEquals("3.testnet.hedera.com:50211", getTopicInfo.getNodeMap().get(AccountId.fromString("0.0.6")));
        assertEquals("0.0.2", getTopicInfo.getOperatorAccountId().toString());
        assertEquals("302e020100300506032b657004220420abb9499694bad1f081cb2a55a08989303cbc3322fae657db1044fdbf3b9eed65", getTopicInfo.getOperatorKey().toString());

        // override defaults
        getTopicInfo
            .withNodeMap(Map.of(AccountId.fromString("0.0.19"),"testing.hedera.com"))
            .withOperatorAccountId(AccountId.fromString("0.0.10"))
            .withOperatorKey(ed25519PrivateKey)
            .setTopicId(ConsensusTopicId.fromString("0.0.44"))
            .setTopicId("0.0.55")
            .setMaxQueryPayment(1000L);
        
        // test updated values
        // note some values are allocated to a Transaction object inside createHCSTopic which don't have getters
        assertEquals(1, getTopicInfo.getNodeMap().size());
        assertEquals("testing.hedera.com", getTopicInfo.getNodeMap().get(AccountId.fromString("0.0.19")));
        assertEquals("0.0.10", getTopicInfo.getOperatorAccountId().toString());
        assertEquals(ed25519PrivateKey.toString(), getTopicInfo.getOperatorKey().toString());
    }
}
