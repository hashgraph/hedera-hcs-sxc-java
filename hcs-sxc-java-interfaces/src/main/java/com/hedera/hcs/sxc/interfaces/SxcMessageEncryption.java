package com.hedera.hcs.sxc.interfaces;
public interface SxcMessageEncryption {
    public   byte[] encrypt(byte[] sharedSecret, byte[] cleartext) throws Exception; 
    public   byte[] decrypt(byte[] sharedSecret, byte[] ciphertext) throws Exception;
}
