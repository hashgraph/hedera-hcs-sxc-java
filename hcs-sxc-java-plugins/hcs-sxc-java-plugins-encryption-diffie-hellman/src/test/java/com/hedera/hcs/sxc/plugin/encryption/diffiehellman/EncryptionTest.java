package com.hedera.hcs.sxc.plugin.encryption.diffiehellman;


import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import java.util.Base64;
import org.junit.jupiter.api.Test;

import com.hedera.hcs.sxc.commonobjects.EncryptedData;
import com.hedera.hcs.sxc.plugin.encryption.diffiehellman.Encryption;
import com.hedera.hcs.sxc.plugin.encryption.diffiehellman.StringUtils;
import java.util.Arrays;

public class EncryptionTest {
    public EncryptionTest() {
        
    }
   
    @Test
    public void testEncryptAndDecrypt() throws Exception {
        byte[] secretKey=null;
        String cleartext = "Hear my cries Hear 234sdf! �$%&*)_+ my call Lend me your ears See my fall See my error Know my faults Time halts See my loss ";

        secretKey = new Encryption().generateSecretKey();

        EncryptedData encrypted = Encryption.load().encrypt(secretKey, StringUtils.stringToByteArray(cleartext));
        EncryptedData encryptedClearText = Encryption.load().encrypt(secretKey, cleartext);
        
       
        assertFalse( Arrays.equals(encrypted.getEncryptedData(), encryptedClearText.getEncryptedData()));
        
        byte[] decrypted = Encryption.load().decrypt(secretKey, encrypted);
        
        assertArrayEquals(StringUtils.stringToByteArray(cleartext), decrypted); 

        String decryptedString = Encryption.load().decryptToString(secretKey, encrypted);
        assertEquals(cleartext, decryptedString); 
    }
    
    @Test
    public void testEncryptAndDecryptWithDifferentKeys() throws Exception {
        byte[] secretKey= new Encryption().generateSecretKey();
        
        String encodeToString = Base64.getEncoder().encodeToString(secretKey);
        String byteArrayToHexString = StringUtils.byteArrayToHexString(secretKey);
        
        String cleartext = "Hear my cries Hear 234sdf! �$%&*)_+ my call Lend me your ears See my fall See my error Know my faults Time halts See my loss ";
        
        byte[] aliceDianaSecret = StringUtils.hexStringToByteArray("0123456789ABCDEF0123456789ABCDEF0123456789ABCDEF0123456789ABCDEF");
        byte[] aliceBobSecret =   StringUtils.hexStringToByteArray("0123456789ABCDEF0123456789ABCDEF0123456789ABCDEF0123456789ABCDEF");
        
        EncryptedData encrypted = Encryption.load().encrypt(aliceDianaSecret, StringUtils.stringToByteArray(cleartext));
        EncryptedData encryptedClearText = Encryption.load().encrypt(aliceDianaSecret, cleartext);
        
        assertFalse( Arrays.equals(encrypted.getEncryptedData(), encryptedClearText.getEncryptedData()));
        
        byte[] decrypted = Encryption.load().decrypt(aliceBobSecret, encrypted);
        
        assertArrayEquals(StringUtils.stringToByteArray(cleartext), decrypted); 

        String decryptedString = Encryption.load().decryptToString(aliceBobSecret, encrypted);
        assertEquals(cleartext, decryptedString); 
    }

}
