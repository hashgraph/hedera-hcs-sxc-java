package com.hedera.hcslib.cryptography;

import com.hedera.hcslib.utils.StringUtils;
import java.security.KeyPair;
import java.util.Arrays;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

public class CryptographyTest {
    
    
    
    
    static byte[] sharedSecret;
    String cleartext = "Hear my cries Hear 234sdf! £$%&*)_+ my call Lend me your ears See my fall See my error Know my faults Time halts See my loss ";
    KeyPair generateRsaKeyPair = null;
    byte[] privateKeyBytes = null;
    String privateKeyHexEncoded = null;
    byte[] publicKeyBytes = null;
    String publicKeyKexEncoded = null;
    
    public CryptographyTest() {
    }

    @BeforeClass
    public static void initCalss(){
        KeyRotation keyRotation = new KeyRotation();
        byte[] alicePublic = keyRotation.aliceFirst();
        Pair<byte[], byte[]> bobPubSecret = KeyRotation.bobGenFromAlice(alicePublic);
        byte[] aliceSharedSecret = keyRotation.aliceFinish(bobPubSecret.getLeft());
        byte[] bobSharedSecret = bobPubSecret.getRight();
        Assert.assertTrue(Arrays.equals(aliceSharedSecret, bobSharedSecret));
        sharedSecret = aliceSharedSecret;
    }
    
    
    @Test
    public void testEncryptAndDecrypt() throws Exception {
        byte[] encrypt = Cryptography.encrypt(sharedSecret, cleartext);
        String encryptHex = StringUtils.byteArrayToHexString(encrypt);
        byte[] encryptPrime = StringUtils.hexStringToByteArray(encryptHex);
        Assert.assertArrayEquals(encrypt, encryptPrime);
        byte[] decrypt = Cryptography.decrypt(sharedSecret, encrypt);
        Assert.assertTrue(Arrays.equals(StringUtils.stringToByteArray(cleartext), decrypt)); 
        Assert.assertEquals(cleartext, StringUtils.byteArrayToString(decrypt));
        Assert.assertEquals(cleartext, Cryptography.decryptToClearText(sharedSecret, encrypt));
    }
}
