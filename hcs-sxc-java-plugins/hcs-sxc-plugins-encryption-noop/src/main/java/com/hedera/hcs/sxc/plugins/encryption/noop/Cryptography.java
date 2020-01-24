package com.hedera.hcs.sxc.plugins.encryption.noop;

import com.hedera.hcs.sxc.interfaces.SxcMessageEncryption;

import lombok.extern.log4j.Log4j2;

@Log4j2
public final class Cryptography implements SxcMessageEncryption {

    @Override
    public byte[] encrypt (byte[] sharedSecret, byte[] cleartext) throws Exception {
        log.info("Received " + cleartext + " to encrypt.");
        return cleartext;
    }

    @Override
    public byte[] decrypt (byte[] sharedSecret, byte[] ciphertext) throws Exception { 
        log.info("Received " + new String(ciphertext) + " to encrypt.");
        return ciphertext;
    }
}
