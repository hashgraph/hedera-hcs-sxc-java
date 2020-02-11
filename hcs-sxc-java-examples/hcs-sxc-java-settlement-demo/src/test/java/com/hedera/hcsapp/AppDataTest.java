package com.hedera.hcsapp;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

public class AppDataTest {    
    
    @Test
    public void testInstantiation() throws Exception {
        AppData appData = new AppData(0, "./src/test/resources/config.yaml", "./src/test/resources/dotenv.sample", "./src/test/resources/docker-compose.yml");
        assertEquals(4, appData.getAppClients().size());
        assertEquals(0, appData.getAppId());
        assertEquals("302a300506032b6570032100bcabbb31c4c6418ea323e02dd46c060f82936141b5d2d2a1da89e59e1267ab6b", appData.getPublicKey());
        assertEquals(0, appData.getTopicIndex());
        assertEquals("Alice", appData.getUserName());
        assertEquals(8081, appData.getWebPort());
        assertNotNull(appData.getHCSCore());
    }
}
