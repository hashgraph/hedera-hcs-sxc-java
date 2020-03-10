package com.hedera.hcs.sxc.plugin.encryption.diffiehellman;
      
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

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.Security;
import javax.crypto.BadPaddingException;

import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;

import javax.crypto.spec.GCMParameterSpec;

public class Encryption implements SxcMessageEncryption {
    
     public static Encryption load(){
         return new Encryption();
     }
     
     public void Encryption(){
         Security.setProperty("crypto.policy", "unlimited");
     }
    
     /**
      * Encrypt a cleartext message using AES and a shared secret generated using a 
      * Diffie Hellman compatible secret 
      * @param sharedSecret A shared secret 
      * @param byte[] cleartext
      * @return the ciphertext
      * @throws NoSuchAlgorithmException
      * @throws NoSuchPaddingException
      * @throws InvalidKeyException
      * @throws IllegalBlockSizeException
      * @throws BadPaddingException
      * @throws UnsupportedEncodingException
      * @throws InvalidAlgorithmParameterException
      */
    @Override
    public  byte[] encrypt(byte[] sharedSecret, byte[] cleartext) 
            throws NoSuchAlgorithmException, 
            NoSuchPaddingException, 
            InvalidKeyException, 
            IllegalBlockSizeException, 
            BadPaddingException, 
            UnsupportedEncodingException, 
            InvalidAlgorithmParameterException{
        if (sharedSecret.length!=32) throw new IllegalArgumentException("Key must be 32 bytes long");
        SecretKeySpec aesKey = new SecretKeySpec(sharedSecret, "AES");
        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        cipher.init(Cipher.ENCRYPT_MODE, aesKey, new GCMParameterSpec(128, new byte[16]));
        byte[] ciphertext = cipher.doFinal(cleartext);
        return ciphertext;
    }
    
    /**
     * Encrypt a cleartext message using AES and a shared secret generated using a 
     * Diffie Hellman compatible secret 
     * @param sharedSecret A shared secret 
     * @param String cleartext
     * @return the ciphertext
     * @throws NoSuchAlgorithmException
     * @throws NoSuchPaddingException
     * @throws InvalidKeyException
     * @throws IllegalBlockSizeException
     * @throws BadPaddingException
     * @throws UnsupportedEncodingException
     * @throws InvalidAlgorithmParameterException
     */
    @Override    
    public byte[] encrypt(byte[] sharedSecret, String cleartext)
            throws NoSuchAlgorithmException, 
            NoSuchPaddingException, 
            InvalidKeyException, 
            IllegalBlockSizeException, 
            BadPaddingException, 
            UnsupportedEncodingException, 
            InvalidAlgorithmParameterException{
        return encrypt(sharedSecret, StringUtils.stringToByteArray(cleartext));
    }
    
    /**
     * Decrypt a ciphertext  with a shared secret generated using a 
     * Diffie Hellman compatible secret
     * @param sharedSecret
     * @param byte[] ciphertext
     * @return the cleartext
     * @throws NoSuchAlgorithmException
     * @throws NoSuchPaddingException
     * @throws InvalidKeyException
     * @throws InvalidAlgorithmParameterException
     * @throws IllegalBlockSizeException
     * @throws BadPaddingException
     * @throws IOException 
     */
    @Override
    public byte[] decrypt(byte[] sharedSecret, byte[] ciphertext) 
            throws 
            NoSuchAlgorithmException, 
            NoSuchPaddingException, 
            InvalidKeyException, 
            InvalidAlgorithmParameterException, 
            IllegalBlockSizeException, 
            BadPaddingException,
            IOException
    {
        SecretKeySpec aesKey = new SecretKeySpec(sharedSecret, "AES"); 
        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        cipher.init(Cipher.DECRYPT_MODE, aesKey, new GCMParameterSpec(128, new byte[16]));
        return cipher.doFinal(ciphertext);  
    }
    
    /**
     * Decrypts using {@link #decrypt(byte[], byte[]) and converts result into to 
     * human readable string. 
     * @param sharedSecret
     * @param ciphertext
     * @return cleartext String
     * @throws NoSuchAlgorithmException
     * @throws NoSuchAlgorithmException
     * @throws NoSuchPaddingException
     * @throws InvalidKeyException
     * @throws InvalidAlgorithmParameterException
     * @throws IllegalBlockSizeException
     * @throws BadPaddingException
     * @throws IOException 
     */
    @Override
    public String decryptToString(byte[] sharedSecret, byte[] ciphertext) 
            throws 
            NoSuchAlgorithmException,
            NoSuchAlgorithmException, 
            NoSuchPaddingException, 
            InvalidKeyException, 
            InvalidAlgorithmParameterException, 
            IllegalBlockSizeException, 
            BadPaddingException,
            IOException
    {
        return StringUtils.byteArrayToString(decrypt(sharedSecret, ciphertext));
    }
    
    /**
     * Generate a fresh RSA {@link KeyPair}
     * @return the KeyPair
     * @throws Exception 
     */
    public static byte[] generateSecretKey() throws Exception {
        KeyGenerator generator = KeyGenerator.getInstance("AES");
        generator.init(256);
        //KeyPair pair = generator.generateKeyPair();
        return generator.generateKey().getEncoded();
    }
    
 }
