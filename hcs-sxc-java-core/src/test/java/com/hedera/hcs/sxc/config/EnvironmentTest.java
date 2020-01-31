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
import org.junit.jupiter.api.Test;

import com.hedera.hcs.sxc.config.Environment;

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
