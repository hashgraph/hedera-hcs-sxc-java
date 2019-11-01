package com.hedera.hcslib.messages;

import com.hedera.hcslib.cryptography.Cryptography;
import com.hedera.hcslib.cryptography.KeyRotation;
import com.hedera.hcslib.hashing.Hashing;
import com.hedera.hcslib.signing.Signing;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import java.security.KeyPair;
import java.util.Arrays;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class HCSMessageOldTest {
    
    static byte[] sharedSecret;
    static byte[] publicKeyBytes;
    static byte[] privateKeyBytes;
    static KeyPair rsaKeyPair;
    //TODO: clear text to include non ascii characters
//    String cleartext = "Hear my cries Hear 234sdf! �$%&*)_+ my call Lend me your ears See my fall See my error Know my faults Time halts See my loss ";
    String cleartext = "Hear my cries Hear 234sdf! $%&*)_+ my call Lend me your ears See my fall See my error Know my faults Time halts See my loss ";
    
    public HCSMessageOldTest() {
    }

    @BeforeAll
    public static void initClass() throws Exception{
        
        /*
            Create a shared key using the KeyRotation tool
        */
        
        KeyRotation keyRotation = new KeyRotation();
        byte[] alicePublic = keyRotation.aliceFirst();
        Pair<byte[], byte[]> bobPubSecret = KeyRotation.bobGenFromAlice(alicePublic);
        byte[] bobPublic = bobPubSecret.getLeft();
        byte[] bobSharedSecret = bobPubSecret.getRight(); 
        byte[] aliceSharedSecret = keyRotation.aliceFinish(bobPublic);
        assertTrue(Arrays.equals(aliceSharedSecret, bobSharedSecret));
        sharedSecret = aliceSharedSecret;
        
        /*
            Create message signing keys.
        */
        
        rsaKeyPair = Cryptography.generateRsaKeyPair();
        publicKeyBytes = rsaKeyPair.getPublic().getEncoded();
        privateKeyBytes = rsaKeyPair.getPrivate().getEncoded();
    }
    
    
    @Test 
    public void encryptDecryptHCSMessageTest(){
        /*
            Encrypt the clear text  message and test if payload meets requirements
        */
        HCSMessageOld a = HCSMessageOld.prepareMessage(sharedSecret, 3, 4, this.cleartext, rsaKeyPair.getPrivate());
        
        // can we identify the signer while message encrypted?
        assertTrue(Signing.verify(a.payload.message, a.payload.signatureOfEncryptedMessage, rsaKeyPair.getPublic()));
       
        
        /*
            Decrypt the previously encrypted and broken up message and test if the 
            requirements are still met. 
        */
        
        HCSMessageOld b = HCSMessageOld.processMessage(sharedSecret, Arrays.asList(a.parts));
        
        // is the stored hash of the original message unaffected?
        assertArrayEquals(Hashing.sha(this.cleartext), a.payload.hashOfUnencryptedMessage);
        
        // is the hash of the decrypted message the same as the hash of the original?
        assertTrue(
                Hashing.matchSHA(
                        Hashing.sha(cleartext), 
                        Hashing.sha(b.payload.message)  
                )
        );  
        
        // does the decryption work?
        assertEquals(this.cleartext, b.payload.message);
            
    } 
    
}
