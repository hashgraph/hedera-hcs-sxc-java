package com.hedera.hcslib.messages;

import com.hedera.hashgraph.sdk.account.CryptoTransferTransaction;
import com.hedera.hcslib.cryptography.Cryptography;
import com.hedera.hcslib.cryptography.KeyRotation;
import com.hedera.hcslib.hashing.Hashing;
import com.hedera.hcslib.signing.Signing;
import java.security.KeyPair;
import java.util.Arrays;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;


public class HCSMessageTest {
    
    static byte[] sharedSecret;
    static byte[] publicKeyBytes;
    static byte[] privateKeyBytes;
    static KeyPair rsaKeyPair;
    String cleartext = "Hear my cries Hear 234sdf! £$%&*)_+ my call Lend me your ears See my fall See my error Know my faults Time halts See my loss ";
    
    public HCSMessageTest() {
    }

    @Before
    public  void initCalss() throws Exception{
        
        /*
            Create a shared key using the KeyRotation tool
        */
        
        KeyRotation keyRotation = new KeyRotation();
        byte[] alicePublic = keyRotation.aliceFirst();
        Pair<byte[], byte[]> bobPubSecret = KeyRotation.bobGenFromAlice(alicePublic);
        byte[] bobPublic = bobPubSecret.getLeft();
        byte[] bobSharedSecret = bobPubSecret.getRight(); 
        byte[] aliceSharedSecret = keyRotation.aliceFinish(bobPublic);
        Assert.assertTrue(Arrays.equals(aliceSharedSecret, bobSharedSecret));
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
        try {
            /*
                Encrypt the clear text  message and test if payload meets requirements
            */
            HCSMessage a = HCSMessage.prepareMessage(sharedSecret, 3, 4, this.cleartext, rsaKeyPair.getPrivate());
            
            // can we identify the signer while message encrypted?
            Assert.assertTrue(Signing.verify(a.payload.message, a.payload.signatureOfEncryptedMessage, rsaKeyPair.getPublic()));
           
            
            /*
                Decrypt the previously encrypted and broken up message and test if the 
                requriements are still met. 
            */
            
            HCSMessage b = HCSMessage.processMessage(sharedSecret, Arrays.asList(a.parts));
            
            // is the stored hash of the original message unaffected?
            Assert.assertArrayEquals(a.payload.hashOfUnencryptedMessage, Hashing.sha(this.cleartext));
            
            // is the hash of the decrypted message the same as the hash of the original?
            Assert.assertTrue(
                    Hashing.matchSHA(
                            Hashing.sha(cleartext), 
                            Hashing.sha(b.payload.message)  
                    )
            );  
            
            // does the decryption work?
            Assert.assertEquals(b.payload.message, this.cleartext);
            
        } catch (Exception ex) {
            Assert.fail();
        } 
    } 
    
}
