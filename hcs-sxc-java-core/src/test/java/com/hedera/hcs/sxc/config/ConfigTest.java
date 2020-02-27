package com.hedera.hcs.sxc.config;

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

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import com.hedera.hashgraph.sdk.account.AccountId;
import com.hedera.hcs.sxc.config.AppNet;
import com.hedera.hcs.sxc.config.Config;
import com.hedera.hcs.sxc.config.Node;
import com.hedera.hcs.sxc.config.YAMLConfig;

public class ConfigTest extends AbstractConfigTest {

    private static Config config;
    private static YAMLConfig yamlConfig;
    private static AppNet appNet;
    private static List<Node> nodeList;
    private static Map<AccountId, String> nodeMap;
    private static long hcsTransactionFee;

    @Test
    public void testLoadConfig() throws Exception {
        config = new Config("./src/test/resources/config.yaml");
        yamlConfig = config.getConfig();
        appNet = yamlConfig.getAppNet();
        nodeList = yamlConfig.getNodes();
        nodeMap = yamlConfig.getNodesMap();
        hcsTransactionFee = yamlConfig.getHCSTransactionFee();

        assertAll(
                () -> assertFalse(appNet.getSignMessages()),
                 () -> assertFalse(appNet.getEncryptMessages()),
                 () -> assertFalse(appNet.getRotateKeys()),
                 () -> assertEquals(0, appNet.getRotateKeyFrequency()),
                 () -> assertEquals(2, appNet.getTopics().size()),
                 () -> assertEquals("0.0.998", appNet.getTopics().get(0).getTopic()),
                 () -> assertEquals("0.0.999", appNet.getTopics().get(1).getTopic()),
                 () -> assertEquals("302e020100300506032b657004220420b89e42e2e4c065f47fddde76b4784918da84012139f030866e1808bf2b3d5ef1", appNet.getTopics().get(0).getSubmitKey()),
                 () -> assertEquals(4, nodeList.size()),
                 () -> assertEquals("0.testnet.hedera.com:50211", nodeList.get(0).getAddress()),
                 () -> assertEquals("0.0.3", nodeList.get(0).getAccount()),
                 () -> assertEquals("1.testnet.hedera.com:50211", nodeList.get(1).getAddress()),
                 () -> assertEquals("0.0.4", nodeList.get(1).getAccount()),
                 () -> assertEquals(4, nodeMap.size()),
                 () -> assertEquals("0.testnet.hedera.com:50211", nodeMap.get(AccountId.fromString("0.0.3"))),
                 () -> assertEquals("1.testnet.hedera.com:50211", nodeMap.get(AccountId.fromString("0.0.4"))),
                 () -> assertEquals("2.testnet.hedera.com:50211", nodeMap.get(AccountId.fromString("0.0.5"))),
                 () -> assertEquals("3.testnet.hedera.com:50211", nodeMap.get(AccountId.fromString("0.0.6"))),
                 () -> assertEquals(100000000, hcsTransactionFee),
                 () -> assertEquals("FULL", appNet.getPersistenceLevel().name()),
                 () -> assertTrue(appNet.getCatchupHistory()),
                 () -> assertEquals("hcs.testnet.mirrornode.hedera.com:5600", yamlConfig.getMirrorNode().getAddress()),
                 () -> assertEquals("org.h2.Driver", yamlConfig.getCoreHibernate().get("hibernate.connection.driver_class"))
         );
    }
}
