package com.hedera.hcsapp;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class AppDataTest {    
    
    @Test
    public void instantiation() throws Exception {
        AppData appData = new AppData("./src/test/resources/config.yaml.", "./src/test/resources/dotenv.sample");
        assertEquals(4, appData.getAppClients().size());
        assertEquals(1, appData.getAppId());
        assertEquals("302a300506032b6570032100bcabbb31c4c6418ea323e02dd46c060f82936141b5d2d2a1da89e59e1267ab6a", appData.getPublicKey());
        assertEquals(0, appData.getTopicIndex());
        assertEquals("Bob", appData.getUserName());
        assertEquals(8082, appData.getWebPort());
        assertNotNull(appData.getHCSCore());
    }
}
