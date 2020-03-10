package com.hedera.hcs.sxc.hashing;

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

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.security.NoSuchAlgorithmException;

import org.junit.jupiter.api.Test;

import com.hedera.hcs.sxc.hashing.Hashing;
import com.hedera.hcs.sxc.utils.StringUtils;

import lombok.extern.log4j.Log4j2;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Log4j2
public class HashingTest {
    
    final String plaintext = "Welcome to the Hedera Hashgraph Concensus Service";
    final String hexOfHashOfPlainText="a9182d102bb8a40cc99ddecfb0176bd0b21363e149fde28f1eafa0307c5599141a5d643a582072a8b6ae805b560f84cf";

    @Test
    public void instance() {
        Hashing h = new Hashing();
        assertTrue(h!=null);
    }
    
    @Test
    public void testSha() throws NoSuchAlgorithmException {
        log.debug("sha");
        byte[] expResult = StringUtils.hexStringToByteArray(hexOfHashOfPlainText);
        byte[] result = Hashing.sha(plaintext);
        String byteArrayToHexString = StringUtils.byteArrayToHexString(result);
        assertArrayEquals(expResult, result);
    }

    @Test
    public void testMatchSHA() throws NoSuchAlgorithmException {
        byte[] sha1 = StringUtils.hexStringToByteArray(hexOfHashOfPlainText);
        byte[] sha2 = Hashing.sha(plaintext);
        boolean expResult = true;
        boolean result = Hashing.matchSHA(sha1, sha2);
        assertEquals(expResult, result);
    }

    @Test
    public void testVerifySHA() throws NoSuchAlgorithmException {
        byte[] sha = Hashing.sha(plaintext);
        String _plaintext = this.plaintext;
        boolean expResult = true;
        boolean result = Hashing.verifySHA(sha, _plaintext);
        assertEquals(expResult, result);
    }
    
}
