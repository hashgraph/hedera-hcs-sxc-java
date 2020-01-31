package com.hedera.hcs.sxc.cryptography;

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

import com.hedera.hcs.sxc.cryptography.Cryptography;
import com.hedera.hcs.sxc.cryptography.KeyRotation;
import com.hedera.hcs.sxc.utils.StringUtils;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.security.KeyPair;
import java.util.Arrays;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class CryptographyTest {
    
    static byte[] sharedSecret;
    String cleartext = "Hear my cries Hear 234sdf! �$%&*)_+ my call Lend me your ears See my fall See my error Know my faults Time halts See my loss ";
    KeyPair generateRsaKeyPair = null;
    byte[] privateKeyBytes = null;
    String privateKeyHexEncoded = null;
    byte[] publicKeyBytes = null;
    String publicKeyKexEncoded = null;
    
    public CryptographyTest() {
    }

    @BeforeAll
    public static void initClass(){
        KeyRotation keyRotation = new KeyRotation();
        byte[] alicePublic = keyRotation.aliceFirst();
        Pair<byte[], byte[]> bobPubSecret = KeyRotation.bobGenFromAlice(alicePublic);
        byte[] aliceSharedSecret = keyRotation.aliceFinish(bobPubSecret.getLeft());
        byte[] bobSharedSecret = bobPubSecret.getRight();
        assertTrue(Arrays.equals(aliceSharedSecret, bobSharedSecret));
        sharedSecret = aliceSharedSecret;
    }
    
    
    @Test
    public void encryptAndDecrypt() throws Exception {
        byte[] encrypt = Cryptography.encrypt(sharedSecret, cleartext);
        String encryptHex = StringUtils.byteArrayToHexString(encrypt);
        byte[] encryptPrime = StringUtils.hexStringToByteArray(encryptHex);
        assertArrayEquals(encrypt, encryptPrime);
        byte[] decrypt = Cryptography.decrypt(sharedSecret, encrypt);
        assertTrue(Arrays.equals(StringUtils.stringToByteArray(cleartext), decrypt)); 
        assertEquals(cleartext, StringUtils.byteArrayToString(decrypt));
        assertEquals(cleartext, Cryptography.decryptToClearText(sharedSecret, encrypt));
    }
}
