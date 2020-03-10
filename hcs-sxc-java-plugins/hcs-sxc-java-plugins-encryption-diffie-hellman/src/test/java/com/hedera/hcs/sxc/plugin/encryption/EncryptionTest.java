package com.hedera.hcs.sxc.plugin.encryption;


import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import java.util.Base64;
import org.junit.jupiter.api.Test;

import com.hedera.hcs.sxc.plugin.encryption.diffiehellman.Encryption;
import com.hedera.hcs.sxc.plugin.encryption.diffiehellman.StringUtils;

public class EncryptionTest {
    public EncryptionTest() {
        
    }
   
    @Test
    public void testEncryptAndDecrypt() throws Exception {
        byte[] secretKey=null;
        String cleartext = "Hear my cries Hear 234sdf! �$%&*)_+ my call Lend me your ears See my fall See my error Know my faults Time halts See my loss ";

        secretKey =  Encryption.generateSecretKey();

        byte[] encrypt = Encryption.load().encrypt(secretKey, StringUtils.stringToByteArray(cleartext));
        byte[] encryptClearText = Encryption.load().encrypt(secretKey, cleartext);
        
        assertArrayEquals(encrypt, encryptClearText);
        
        byte[] decrypt = Encryption.load().decrypt(secretKey, encrypt);
        
        assertArrayEquals(StringUtils.stringToByteArray(cleartext), decrypt); 

        String decryptString = Encryption.load().decryptToString(secretKey, encrypt);
        assertEquals(cleartext, decryptString); 
    }
    
    @Test
    public void testEncryptAndDecryptWithDifferentKeys() throws Exception {
        byte[] secretKey= Encryption.generateSecretKey();
        
        String encodeToString = Base64.getEncoder().encodeToString(secretKey);
        String byteArrayToHexString = StringUtils.byteArrayToHexString(secretKey);
        
        String cleartext = "Hear my cries Hear 234sdf! �$%&*)_+ my call Lend me your ears See my fall See my error Know my faults Time halts See my loss ";
        
        byte[] aliceDianaSecret = StringUtils.hexStringToByteArray("0123456789ABCDEF0123456789ABCDEF0123456789ABCDEF0123456789ABCDEF");
        byte[] aliceBobSecret =   StringUtils.hexStringToByteArray("0123456789ABCDEF0123456789ABCDEF0123456789ABCDEF0123456789ABCDEF");
        
        
        byte[] encrypt = Encryption.load().encrypt(aliceDianaSecret, StringUtils.stringToByteArray(cleartext));
        byte[] encryptClearText = Encryption.load().encrypt(aliceDianaSecret, cleartext);
        
        assertArrayEquals(encrypt, encryptClearText);
        
        byte[] decrypt = Encryption.load().decrypt(aliceBobSecret, encrypt);
        
        assertArrayEquals(StringUtils.stringToByteArray(cleartext), decrypt); 

        String decryptString = Encryption.load().decryptToString(aliceBobSecret, encrypt);
        assertEquals(cleartext, decryptString); 
    }

}
