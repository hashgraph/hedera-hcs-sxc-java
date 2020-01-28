package com.hedera.hcs.sxc.plugin.cryptography.cryptography;
import com.hedera.hcs.sxc.utils.StringUtils;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.security.KeyPair;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class CryptographyTest {
    
    static byte[] secretKey;
    String cleartext = "Hear my cries Hear 234sdf! �$%&*)_+ my call Lend me your ears See my fall See my error Know my faults Time halts See my loss ";
    KeyPair generateRsaKeyPair = null;
    byte[] privateKeyBytes = null;
    String privateKeyHexEncoded = null;
    byte[] publicKeyBytes = null;
    String publicKeyKexEncoded = null;
    
    public CryptographyTest() {
    }

    @BeforeAll
    public static void initClass(){
        try {
            KeyPair kp = Cryptography.generateRsaKeyPair();
            secretKey =  kp.getPrivate().getEncoded();
            //System.out.println(StringUtils.byteArrayToHexString(secretKey));
        } catch (Exception ex) {
            Logger.getLogger(CryptographyTest.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    
    @Test
    public void encryptAndDecrypt() throws Exception {
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
