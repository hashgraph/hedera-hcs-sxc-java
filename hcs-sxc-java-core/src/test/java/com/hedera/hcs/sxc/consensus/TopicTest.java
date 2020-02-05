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
   
    public TopicTest() {
    }
    
    @Test
    public void createTopic() throws Exception {
        Ed25519PrivateKey ed25519PrivateKey = Ed25519PrivateKey.generate();
        Ed25519PrivateKey adminKey = Ed25519PrivateKey.generate();
        Ed25519PrivateKey submitKey = Ed25519PrivateKey.generate();
        HCSCore hcsCore = new HCSCore(0, "./src/test/resources/config.yaml", "./src/test/resources/dotenv.test");

        CreateHCSTopic createHCSTopic = new CreateHCSTopic(hcsCore);

        assertEquals(4, createHCSTopic.getOverrideNodeMap().size());
        assertEquals("0.testnet.hedera.com:50211", createHCSTopic.getOverrideNodeMap().get(AccountId.fromString("0.0.3")));
        assertEquals("1.testnet.hedera.com:50211", createHCSTopic.getOverrideNodeMap().get(AccountId.fromString("0.0.4")));
        assertEquals("2.testnet.hedera.com:50211", createHCSTopic.getOverrideNodeMap().get(AccountId.fromString("0.0.5")));
        assertEquals("3.testnet.hedera.com:50211", createHCSTopic.getOverrideNodeMap().get(AccountId.fromString("0.0.6")));
        assertEquals("0.0.2", createHCSTopic.getOverrideOperatorAccountId().toString());
        assertEquals("302e020100300506032b657004220420abb9499694bad1f081cb2a55a08989303cbc3322fae657db1044fdbf3b9eed65", createHCSTopic.getOverrideOperatorKey().toString());

        // override defaults
        createHCSTopic
            .overrideNodeMap(Map.of(AccountId.fromString("0.0.19"),"testing.hedera.com"))
            .overrideOperatorAccountId(AccountId.fromString("0.0.10"))
            .overrideOperatorKey(ed25519PrivateKey)
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
        assertEquals(1, createHCSTopic.getOverrideNodeMap().size());
        assertEquals("testing.hedera.com", createHCSTopic.getOverrideNodeMap().get(AccountId.fromString("0.0.19")));
        assertEquals("0.0.10", createHCSTopic.getOverrideOperatorAccountId().toString());
        assertEquals(ed25519PrivateKey.toString(), createHCSTopic.getOverrideOperatorKey().toString());
    }

    @Test
    public void updateTopic() throws Exception {
        Ed25519PrivateKey ed25519PrivateKey = Ed25519PrivateKey.generate();
        Ed25519PrivateKey adminKey = Ed25519PrivateKey.generate();
        Ed25519PrivateKey submitKey = Ed25519PrivateKey.generate();
        HCSCore hcsCore = new HCSCore(0, "./src/test/resources/config.yaml", "./src/test/resources/dotenv.test");

        UpdateHCSTopic updateHCSTopic = new UpdateHCSTopic(hcsCore);

        assertEquals(4, updateHCSTopic.getOverrideNodeMap().size());
        assertEquals("0.testnet.hedera.com:50211", updateHCSTopic.getOverrideNodeMap().get(AccountId.fromString("0.0.3")));
        assertEquals("1.testnet.hedera.com:50211", updateHCSTopic.getOverrideNodeMap().get(AccountId.fromString("0.0.4")));
        assertEquals("2.testnet.hedera.com:50211", updateHCSTopic.getOverrideNodeMap().get(AccountId.fromString("0.0.5")));
        assertEquals("3.testnet.hedera.com:50211", updateHCSTopic.getOverrideNodeMap().get(AccountId.fromString("0.0.6")));
        assertEquals("0.0.2", updateHCSTopic.getOverrideOperatorAccountId().toString());
        assertEquals("302e020100300506032b657004220420abb9499694bad1f081cb2a55a08989303cbc3322fae657db1044fdbf3b9eed65", updateHCSTopic.getOverrideOperatorKey().toString());

        // override defaults
        updateHCSTopic
            .overrideNodeMap(Map.of(AccountId.fromString("0.0.19"),"testing.hedera.com"))
            .overrideOperatorAccountId(AccountId.fromString("0.0.10"))
            .overrideOperatorKey(ed25519PrivateKey)
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
        assertEquals(1, updateHCSTopic.getOverrideNodeMap().size());
        assertEquals("testing.hedera.com", updateHCSTopic.getOverrideNodeMap().get(AccountId.fromString("0.0.19")));
        assertEquals("0.0.10", updateHCSTopic.getOverrideOperatorAccountId().toString());
        assertEquals(ed25519PrivateKey.toString(), updateHCSTopic.getOverrideOperatorKey().toString());
    }

    @Test
    public void deleteTopic() throws Exception {
        Ed25519PrivateKey ed25519PrivateKey = Ed25519PrivateKey.generate();
        HCSCore hcsCore = new HCSCore(0, "./src/test/resources/config.yaml", "./src/test/resources/dotenv.test");

        DeleteHCSTopic deleteHCSTopic = new DeleteHCSTopic(hcsCore);

        assertEquals(4, deleteHCSTopic.getOverrideNodeMap().size());
        assertEquals("0.testnet.hedera.com:50211", deleteHCSTopic.getOverrideNodeMap().get(AccountId.fromString("0.0.3")));
        assertEquals("1.testnet.hedera.com:50211", deleteHCSTopic.getOverrideNodeMap().get(AccountId.fromString("0.0.4")));
        assertEquals("2.testnet.hedera.com:50211", deleteHCSTopic.getOverrideNodeMap().get(AccountId.fromString("0.0.5")));
        assertEquals("3.testnet.hedera.com:50211", deleteHCSTopic.getOverrideNodeMap().get(AccountId.fromString("0.0.6")));
        assertEquals("0.0.2", deleteHCSTopic.getOverrideOperatorAccountId().toString());
        assertEquals("302e020100300506032b657004220420abb9499694bad1f081cb2a55a08989303cbc3322fae657db1044fdbf3b9eed65", deleteHCSTopic.getOverrideOperatorKey().toString());

        // override defaults
        deleteHCSTopic
            .overrideNodeMap(Map.of(AccountId.fromString("0.0.19"),"testing.hedera.com"))
            .overrideOperatorAccountId(AccountId.fromString("0.0.10"))
            .overrideOperatorKey(ed25519PrivateKey)
            .setMaxTransactionFee(100L)
            .setTopicId(ConsensusTopicId.fromString("0.0.44"))
            .setTopicId("0.0.55")
            .setTransactionMemo("Transaction Memo for test");
        
        // test updated values
        // note some values are allocated to a Transaction object inside createHCSTopic which don't have getters
        assertEquals(1, deleteHCSTopic.getOverrideNodeMap().size());
        assertEquals("testing.hedera.com", deleteHCSTopic.getOverrideNodeMap().get(AccountId.fromString("0.0.19")));
        assertEquals("0.0.10", deleteHCSTopic.getOverrideOperatorAccountId().toString());
        assertEquals(ed25519PrivateKey.toString(), deleteHCSTopic.getOverrideOperatorKey().toString());
    }

    @Test
    public void infoTopic() throws Exception {
        Ed25519PrivateKey ed25519PrivateKey = Ed25519PrivateKey.generate();
        HCSCore hcsCore = new HCSCore(0, "./src/test/resources/config.yaml", "./src/test/resources/dotenv.test");

        GetTopicInfo getTopicInfo = new GetTopicInfo(hcsCore);

        assertEquals(4, getTopicInfo.getOverrideNodeMap().size());
        assertEquals("0.testnet.hedera.com:50211", getTopicInfo.getOverrideNodeMap().get(AccountId.fromString("0.0.3")));
        assertEquals("1.testnet.hedera.com:50211", getTopicInfo.getOverrideNodeMap().get(AccountId.fromString("0.0.4")));
        assertEquals("2.testnet.hedera.com:50211", getTopicInfo.getOverrideNodeMap().get(AccountId.fromString("0.0.5")));
        assertEquals("3.testnet.hedera.com:50211", getTopicInfo.getOverrideNodeMap().get(AccountId.fromString("0.0.6")));
        assertEquals("0.0.2", getTopicInfo.getOverrideOperatorAccountId().toString());
        assertEquals("302e020100300506032b657004220420abb9499694bad1f081cb2a55a08989303cbc3322fae657db1044fdbf3b9eed65", getTopicInfo.getOverrideOperatorKey().toString());

        // override defaults
        getTopicInfo
            .overrideNodeMap(Map.of(AccountId.fromString("0.0.19"),"testing.hedera.com"))
            .overrideOperatorAccountId(AccountId.fromString("0.0.10"))
            .overrideOperatorKey(ed25519PrivateKey)
            .setTopicId(ConsensusTopicId.fromString("0.0.44"))
            .setTopicId("0.0.55")
            .setMaxQueryPayment(1000L);
        
        // test updated values
        // note some values are allocated to a Transaction object inside createHCSTopic which don't have getters
        assertEquals(1, getTopicInfo.getOverrideNodeMap().size());
        assertEquals("testing.hedera.com", getTopicInfo.getOverrideNodeMap().get(AccountId.fromString("0.0.19")));
        assertEquals("0.0.10", getTopicInfo.getOverrideOperatorAccountId().toString());
        assertEquals(ed25519PrivateKey.toString(), getTopicInfo.getOverrideOperatorKey().toString());
    }
}
