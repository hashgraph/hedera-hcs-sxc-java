package com.hedera.hcs.sxc.interfaces;

import com.hedera.hcs.sxc.commonobjects.EncryptedData;
import com.hedera.hcs.sxc.exceptions.SCXCryptographyException;

public interface SxcMessageEncryption {
    /**
     * Encrypt a cleartext message using a shared secret 
     * @param sharedSecret A shared secret 
     * @param byte[] cleartext
     * @return EncryptedData
     * @throws Exception
     */
    public EncryptedData encrypt(byte[] sharedSecret, byte[] cleartext) throws SCXCryptographyException;
    /**
     * Encrypt a cleartext message using a shared secret 
     * @param sharedSecret A shared secret 
     * @param String cleartext
     * @return EncryptedData
     * @throws Exception
     */
    public EncryptedData encrypt(byte[] sharedSecret, String cleartext) throws SCXCryptographyException;
    /**
     * Decrypts cipherText using sharedSecret and random 
     * @param sharedSecret
     * @param encryptedData
     * @return byte[]
     * @throws Exception
     */    
    public byte[] decrypt(byte[] sharedSecret, EncryptedData encryptedData) throws SCXCryptographyException;
    /**
     * Decrypts using {@link #decrypt(byte[], EncryptedData) and converts result into to 
     * human readable string. 
     * @param sharedSecret
     * @param encryptedData
     * @return cleartext String
     * @throws Exception
     */
    public String decryptToString(byte[] sharedSecret, EncryptedData encryptedData) throws SCXCryptographyException; 

    /**
     * Generate a secret key
     * @return the secret key
     * @throws Exception 
     */
    public byte[] generateSecretKey() throws SCXCryptographyException;
}
