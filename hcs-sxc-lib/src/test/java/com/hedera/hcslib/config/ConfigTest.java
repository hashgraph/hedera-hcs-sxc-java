package com.hedera.hcslib.config;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.hedera.hashgraph.sdk.account.AccountId;

public class ConfigTest extends AbstractConfigTest {

    private static Config config;
    private static YAMLConfig yamlConfig;
    private static AppNet appNet;
    private static List<Node> nodeList;
    private static Map<AccountId, String> nodeMap;
    private static long hcsTransactionFee;

    @BeforeEach
    public void init() throws FileNotFoundException, IOException {
        config = new Config();
        yamlConfig = config.getConfig();
        appNet = yamlConfig.getAppNet();
        nodeList = yamlConfig.getNodes();
        nodeMap = yamlConfig.getNodesMap();
        hcsTransactionFee = yamlConfig.getHCSTransactionFee();
    }

    @Test
    public void loadConfig() throws Exception {
        assertAll(
                () -> assertFalse(appNet.getSignMessages()),
                 () -> assertFalse(appNet.getEncryptMessages()),
                 () -> assertFalse(appNet.getRotateKeys()),
                 () -> assertEquals(0, appNet.getRotateKeyFrequency()),
                 () -> assertEquals(2, appNet.getTopics().size()),
                 () -> assertEquals("0.0.1072", appNet.getTopics().get(0).getTopic()),
                 () -> assertEquals("0.0.1088", appNet.getTopics().get(1).getTopic()),
                 () -> assertEquals(2, nodeList.size()),
                 () -> assertEquals("0.testnet.hedera.com:50211", nodeList.get(0).getAddress()),
                 () -> assertEquals("0.0.3", nodeList.get(0).getAccount()),
                 () -> assertEquals("1.testnet.hedera.com:50211", nodeList.get(1).getAddress()),
                 () -> assertEquals("0.0.4", nodeList.get(1).getAccount()),
                 () -> assertEquals(2, nodeMap.size()),
                 () -> assertEquals("0.testnet.hedera.com:50211", nodeMap.get(AccountId.fromString("0.0.3"))),
                 () -> assertEquals("1.testnet.hedera.com:50211", nodeMap.get(AccountId.fromString("0.0.4"))),
                 () -> assertEquals(100000000, hcsTransactionFee),
                 () -> assertEquals("FULL", appNet.getPersistenceLevel().name()),
                 () -> assertTrue(appNet.getCatchupHistory()),
                 () -> assertEquals("34.66.214.12:6552", yamlConfig.getMirrorNode().getAddress()),
                 () -> assertEquals(10, yamlConfig.getMirrorNode().getReconnectDelay())
         );
    }
}
