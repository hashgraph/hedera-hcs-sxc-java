package com.hedera.hcs.sxc.plugin.encryption.diffiehellman;
      
import com.hedera.hcs.sxc.commonobjects.EncryptedData;
import com.hedera.hcs.sxc.interfaces.SxcMessageEncryption;

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

import java.security.KeyPair;
import java.security.SecureRandom;
import java.security.Security;
import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.spec.SecretKeySpec;

import javax.crypto.spec.GCMParameterSpec;

public class Encryption implements SxcMessageEncryption {
    
     public static Encryption load(){
         return new Encryption();
     }
     
     public Encryption(){
         Security.setProperty("crypto.policy", "unlimited");
     }
    
     /**
      * Encrypt a cleartext message using AES and a shared secret generated using a 
      * Diffie Hellman compatible secret 
      * @param sharedSecret A shared secret 
      * @param byte[] cleartext
      * @return EncryptedData
      * @throws Exception
      */
     @Override
     public EncryptedData encrypt(byte[] sharedSecret, byte[] cleartext) throws Exception {
        if (sharedSecret.length!=32) throw new IllegalArgumentException("Key must be 32 bytes long");
        SecretKeySpec aesKey = new SecretKeySpec(sharedSecret, "AES");
        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        SecureRandom random = new SecureRandom();
        byte[] randomIV = new byte[16];
        random.nextBytes(randomIV);
        cipher.init(Cipher.ENCRYPT_MODE, aesKey, new GCMParameterSpec(128, randomIV));
        byte[] ciphertext = cipher.doFinal(cleartext);
        EncryptedData result = new EncryptedData();
        result.setEncryptedData(ciphertext);
        result.setRandom(randomIV);
        return result;
    }
    
     /**
      * Encrypt a cleartext message using AES and a shared secret generated using a 
      * Diffie Hellman compatible secret 
      * @param sharedSecret A shared secret 
      * @param String cleartext
      * @return EncryptedData
      * @throws Exception
      */
     @Override    
     public EncryptedData encrypt(byte[] sharedSecret, String cleartext) throws Exception {
        return encrypt(sharedSecret, StringUtils.stringToByteArray(cleartext));
    }
    
     /**
      * Decrypts cipherText using sharedSecret and random 
      * @param sharedSecret
      * @param encryptedData
      * @return byte[]
      * @throws Exception
      */    
     @Override
     public byte[] decrypt(byte[] sharedSecret, EncryptedData encryptedData) throws Exception {
        SecretKeySpec aesKey = new SecretKeySpec(sharedSecret, "AES"); 
        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        cipher.init(Cipher.DECRYPT_MODE, aesKey, new GCMParameterSpec(128, encryptedData.getRandom()));
        return cipher.doFinal(encryptedData.getEncryptedData());  
    }
    
     /**
      * Decrypts using {@link #decrypt(byte[], EncryptedData) and converts result into to 
      * human readable string. 
      * @param sharedSecret
      * @param encryptedData
      * @return cleartext String
      * @throws Exception
      */
     @Override
     public String decryptToString(byte[] sharedSecret, EncryptedData encryptedData) throws Exception { 
        return StringUtils.byteArrayToString(decrypt(sharedSecret, encryptedData));
    }
    
    /**
     * Generate a fresh RSA {@link KeyPair}
     * @return the KeyPair
     * @throws Exception 
     */
     @Override
    public byte[] generateSecretKey() throws Exception {
        KeyGenerator generator = KeyGenerator.getInstance("AES");
        generator.init(256);
        return generator.generateKey().getEncoded();
    }
    
 }
