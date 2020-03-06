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

import com.hedera.hcs.sxc.plugin.cryptography.StringUtils;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import java.security.KeyPair;
import java.security.Security;
import java.util.Base64;
import org.junit.jupiter.api.Test;

public class CryptographyTest {
    public CryptographyTest() {
        
    }
   
    @Test
    public void testEncryptAndDecrypt() throws Exception {
        byte[] secretKey=null;
        String cleartext = "Hear my cries Hear 234sdf! �$%&*)_+ my call Lend me your ears See my fall See my error Know my faults Time halts See my loss ";

       // KeyPair kp = Cryptography.generateRsaKeyPair();
        secretKey =  Cryptography.generateSecretKey();

        byte[] encrypt = Cryptography.load().encrypt(secretKey, StringUtils.stringToByteArray(cleartext));
        byte[] encryptClearText = Cryptography.load().encryptFromClearText(secretKey, cleartext);
        
        assertArrayEquals(encrypt, encryptClearText);
        
        byte[] decrypt = Cryptography.load().decrypt(secretKey, encrypt);
        
        assertArrayEquals(StringUtils.stringToByteArray(cleartext), decrypt); 

        String decryptString = Cryptography.load().decryptToClearText(secretKey, encrypt);
        assertEquals(cleartext, decryptString); 
    }
    
    @Test
    public void testEncryptAndDecryptWithDifferentKeys() throws Exception {
        byte[] secretKey= Cryptography.generateSecretKey();
        
        String encodeToString = Base64.getEncoder().encodeToString(secretKey);
        String byteArrayToHexString = StringUtils.byteArrayToHexString(secretKey);
        
        String cleartext = "Hear my cries Hear 234sdf! �$%&*)_+ my call Lend me your ears See my fall See my error Know my faults Time halts See my loss ";

        
        byte[] aliceDianaSecret = StringUtils.hexStringToByteArray("0123456789ABCDEF0123456789ABCDEF0123456789ABCDEF0123456789ABCDEF");
        byte[] aliceBobSecret =   StringUtils.hexStringToByteArray("0123456789ABCDEF0123456789ABCDEF0123456789ABCDEF0123456789ABCDEF");
        
        
        byte[] encrypt = Cryptography.load().encrypt(aliceDianaSecret, StringUtils.stringToByteArray(cleartext));
        byte[] encryptClearText = Cryptography.load().encryptFromClearText(aliceDianaSecret, cleartext);
        
        assertArrayEquals(encrypt, encryptClearText);
        
        byte[] decrypt = Cryptography.load().decrypt(aliceBobSecret, encrypt);
        
        assertArrayEquals(StringUtils.stringToByteArray(cleartext), decrypt); 

        String decryptString = Cryptography.load().decryptToClearText(aliceBobSecret, encrypt);
        assertEquals(cleartext, decryptString); 
    }

}
