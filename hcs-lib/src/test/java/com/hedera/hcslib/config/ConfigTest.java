package com.hedera.hcslib.config;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
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
    private static Queue queue;

    @BeforeEach
    public void init() throws FileNotFoundException, IOException {
        config = new Config();
        yamlConfig = config.getConfig();
        appNet = yamlConfig.getAppNet();
        nodeList = yamlConfig.getNodes();
        nodeMap = yamlConfig.getNodesMap();
        hcsTransactionFee = yamlConfig.getHCSTransactionFee();
        queue = yamlConfig.getQueue();
    }

    @Test
    public void loadConfig() throws Exception {
        assertAll(
                () -> assertTrue(appNet.getSignMessages()),
                 () -> assertTrue(appNet.getEncryptMessages()),
                 () -> assertTrue(appNet.getRotateKeys()),
                 () -> assertEquals(1, appNet.getRotateKeyFrequency()),
                 () -> assertEquals(2, appNet.getTopics().size()),
                 () -> assertEquals("0.0.10", appNet.getTopics().get(0).getTopic()),
                 () -> assertEquals("0.0.11", appNet.getTopics().get(1).getTopic()),
                 () -> assertTopicId(0, 0, 10, appNet.getTopicIds().get(0)),
                 () -> assertTopicId(0, 0, 11, appNet.getTopicIds().get(1)),
                 () -> assertEquals(2, nodeList.size()),
                 () -> assertEquals("0.testnet.hedera.com:50211", nodeList.get(0).getAddress()),
                 () -> assertEquals("0.0.3", nodeList.get(0).getAccount()),
                 () -> assertEquals("1.testnet.hedera.com:50211", nodeList.get(1).getAddress()),
                 () -> assertEquals("0.0.4", nodeList.get(1).getAccount()),
                 () -> assertEquals(2, nodeMap.size()),
                 () -> assertEquals("0.testnet.hedera.com:50211", nodeMap.get(AccountId.fromString("0.0.3"))),
                 () -> assertEquals("1.testnet.hedera.com:50211", nodeMap.get(AccountId.fromString("0.0.4"))),
                 () -> assertEquals(100000000, hcsTransactionFee),
                 () -> assertEquals("tcpConnectionFactory", queue.getTcpConnectionFactory()),
                 () -> assertEquals("initialContextFactory", queue.getInitialContextFactory()),
                 () -> assertEquals("topic", queue.getTopic()),
                 () -> assertEquals("vmConnectionFactory", queue.getVmConnectionFactory()),
                 () -> assertEquals("JGroupsConnectionFactory", queue.getJGroupsConnectionFactory())
        );
    }
}
