package com.hedera.hcs.sxc.plugin.cryptography.cryptography;


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


import com.hedera.hcs.sxc.utils.StringUtils;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.security.KeyPair;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.junit.jupiter.api.Test;

public class CryptographyTest {
    
  
    
    public CryptographyTest() {
    }

   
    @Test
    public void testEncryptAndDecrypt() throws Exception {
        byte[] secretKey=null;
        String cleartext = "Hear my cries Hear 234sdf! �$%&*)_+ my call Lend me your ears See my fall See my error Know my faults Time halts See my loss ";
        //KeyPair generateRsaKeyPair = null;
        //byte[] privateKeyBytes = null;
        //String privateKeyHexEncoded = null;
        //byte[] publicKeyBytes = null;
        //String publicKeyKexEncoded = null;
        try {
            KeyPair kp = Cryptography.generateRsaKeyPair();
            secretKey =  kp.getPrivate().getEncoded();
            //System.out.println(StringUtils.byteArrayToHexString(secretKey));
        } catch (Exception ex) {
            Logger.getLogger(CryptographyTest.class.getName()).log(Level.SEVERE, null, ex);
        }
        byte[] encrypt = Cryptography.load().encrypt(secretKey, StringUtils.stringToByteArray(cleartext));
        String encryptHex = StringUtils.byteArrayToHexString(encrypt);
        byte[] encryptPrime = StringUtils.hexStringToByteArray(encryptHex);
        assertArrayEquals(encrypt, encryptPrime);
        byte[] decrypt = Cryptography.load().decrypt(secretKey, encrypt);
        assertTrue(Arrays.equals(StringUtils.stringToByteArray(cleartext), decrypt)); 
        assertEquals(cleartext, StringUtils.byteArrayToString(decrypt));
        assertEquals(cleartext, Cryptography.load().decryptToClearText(secretKey, encrypt));
    }
}
