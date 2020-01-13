package com.hedera.hcs.sxc.plugins.encryption.diffiehellman;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import com.hedera.hcs.sxc.interfaces.SxcMessageEncryption;

public final class Cryptography implements SxcMessageEncryption {

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
    @Override
    public byte[] encrypt (byte[] sharedSecret, byte[] cleartext) throws Exception {
        SecretKeySpec aesKey = new SecretKeySpec(sharedSecret, 0, 16, "AES");
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        cipher.init(Cipher.ENCRYPT_MODE, aesKey,new IvParameterSpec(new byte[16]));
        byte[] ciphertext = cipher.doFinal(cleartext);
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
    @Override
    public byte[] decrypt (byte[] sharedSecret, byte[] ciphertext) throws Exception { 
        SecretKeySpec aesKey = new SecretKeySpec(sharedSecret,0, 16, "AES"); 
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        cipher.init(Cipher.DECRYPT_MODE, aesKey,new IvParameterSpec(new byte[16]));
        return cipher.doFinal(ciphertext);  
    }
}
