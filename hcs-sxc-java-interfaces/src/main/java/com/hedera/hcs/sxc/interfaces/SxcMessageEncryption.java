package com.hedera.hcs.sxc.interfaces;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

public interface SxcMessageEncryption {
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
    public byte[] encrypt(byte[] sharedSecret, byte[] cleartext) throws Exception;
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
    public byte[] encrypt(byte[] sharedSecret, String cleartext) throws Exception;
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
    public byte[] decrypt(byte[] sharedSecret, byte[] ciphertext) throws Exception;
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
    public String decryptToString(byte[] sharedSecret, byte[] ciphertext) throws Exception; 
}
