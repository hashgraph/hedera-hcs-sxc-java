package com.hedera.hcslib.config;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;

public class EnvironmentTest {

    private static Environment environment;

    @Test
    public void loadConfig() throws Exception {
        environment = new Environment("dotenv.test");
        assertAll(
                 () -> assertEquals("302e020100300506032b657004220420abb9499694bad1f081cb2a55a08989303cbc3322fae657db1044fdbf3b9eed65", environment.getOperatorKey().toString()),
                 () -> assertEquals("0.0.2", environment.getOperatorAccount()),
                 () -> assertEquals(10, environment.getAppId())
        );
    }
}
