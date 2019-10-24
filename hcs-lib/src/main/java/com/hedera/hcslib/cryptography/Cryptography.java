package com.hedera.hcslib.cryptography;

import com.hedera.hcslib.utils.StringUtils;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;

import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;


import java.util.logging.Level;
import java.util.logging.Logger;
import javax.crypto.BadPaddingException;

import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;


public class Cryptography {
    
    /**
     * Encrypt a cleartext message using AES and a shared secret generated using the 
     * Diffie Hellman compatible secret obtained via the {@link KeyRotation} utility
     * @param sharedSecret A shared secret obtained from {@link KeyRotation}
     * @param cleartext
     * @return the ciphertext
     * @throws NoSuchAlgorithmException
     * @throws NoSuchPaddingException
     * @throws InvalidKeyException
     * @throws IllegalBlockSizeException
     * @throws BadPaddingException
     * @throws UnsupportedEncodingException
     * @throws InvalidAlgorithmParameterException
     */
    public static byte[] encrypt (byte[] sharedSecret, String cleartext) 
            throws NoSuchAlgorithmException, 
            NoSuchPaddingException, 
            InvalidKeyException, 
            IllegalBlockSizeException, 
            BadPaddingException, 
            UnsupportedEncodingException, 
            InvalidAlgorithmParameterException{
        SecretKeySpec aesKey = new SecretKeySpec(sharedSecret, 0, 16, "AES");
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        cipher.init(Cipher.ENCRYPT_MODE, aesKey,new IvParameterSpec(new byte[16]));
        byte[] ciphertext = cipher.doFinal(StringUtils.stringToByteArray(cleartext));
        return ciphertext;
    }
    
    /**
     * Decrypt a ciphertext  with a shared secret generated using the 
     * Diffie Hellman compatible secret from the {@link KeyRotation} utility
     * @param sharedSecret
     * @param ciphertext
     * @return the cleartext
     * @throws NoSuchAlgorithmException
     * @throws NoSuchPaddingException
     * @throws InvalidKeyException
     * @throws InvalidAlgorithmParameterException
     * @throws IllegalBlockSizeException
     * @throws BadPaddingException
     * @throws IOException 
     */
    public static byte[] decrypt (byte[]  sharedSecret, byte[] ciphertext) 
            throws 
            NoSuchAlgorithmException, 
            NoSuchPaddingException, 
            InvalidKeyException, 
            InvalidAlgorithmParameterException, 
            IllegalBlockSizeException, 
            BadPaddingException,
            IOException
    {
        SecretKeySpec aesKey = new SecretKeySpec(sharedSecret,0, 16, "AES"); 
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        cipher.init(Cipher.DECRYPT_MODE, aesKey,new IvParameterSpec(new byte[16]));
        return cipher.doFinal(ciphertext);  
    }
    
    /**
     * Decrypts using {@link #decrypt(byte[], byte[]) and converts result into to 
     * human readable string. 
     * @param sharedSecret
     * @param ciphertext
     * @return cleartext string
     * @throws NoSuchAlgorithmException
     * @throws NoSuchAlgorithmException
     * @throws NoSuchPaddingException
     * @throws InvalidKeyException
     * @throws InvalidAlgorithmParameterException
     * @throws IllegalBlockSizeException
     * @throws BadPaddingException
     * @throws IOException 
     */
    public static String decryptToClearText(byte[]  sharedSecret, byte[] ciphertext) 
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
    public static KeyPair generateRsaKeyPair() throws Exception {
        KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
        generator.initialize(2048, new SecureRandom());
        KeyPair pair = generator.generateKeyPair();
        return pair;
    }
    
    /**
     * Retrieve a {@link PrivateKey} that has been serialized using {@link #toHexString(java.security.PrivateKey) }
     * @param privateKeyHexEncoded
     * @return  The PrivateKey
     */
    public static PrivateKey fromHexPrivateKey(String privateKeyHexEncoded){
        PrivateKey r = null;
        try {
            byte[] privateKeyBytes = StringUtils.hexStringToByteArray(privateKeyHexEncoded);
            KeyFactory kf = KeyFactory.getInstance("RSA"); // or "EC" or whatever
            r =  kf.generatePrivate(new PKCS8EncodedKeySpec(privateKeyBytes));
        } catch (InvalidKeySpecException | NoSuchAlgorithmException ex) {
            Logger.getLogger(Cryptography.class.getName()).log(Level.SEVERE, null, ex);
        }
        return r;
    }
    
    /**
     * Retrieve a {@link PublicKey} that has been serialized using {@link #toHexString(java.security.PublicKey) }
     * @param publicKeyHexEncoded
     * @return  The PrivateKey
     */
    public static PublicKey fromHexPublicKey(String publicKeyHexEncoded){
        PublicKey r = null;
        try {
            byte[] publicKeyBytes = StringUtils.hexStringToByteArray(publicKeyHexEncoded);
            KeyFactory kf = KeyFactory.getInstance("RSA"); // or "EC" or whatever
            r =  kf.generatePublic(new X509EncodedKeySpec(publicKeyBytes));
        } catch (InvalidKeySpecException | NoSuchAlgorithmException ex) {
            Logger.getLogger(Cryptography.class.getName()).log(Level.SEVERE, null, ex);
        }
        return r;
    }
    
    public static String toHexString (PublicKey publicKey){
        return StringUtils.byteArrayToHexString(publicKey.getEncoded());
    }
    
    public static String toHexString (PrivateKey privateKey){
        return StringUtils.byteArrayToHexString(privateKey.getEncoded());
    }
     

 }
