package com.hedera.hcs.sxc.plugin.cryptography.keyrotation;

import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;

import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;


import javax.crypto.KeyAgreement;

import javax.crypto.interfaces.DHPublicKey;
import javax.crypto.spec.DHParameterSpec;
import org.apache.commons.lang3.tuple.Pair;

import lombok.extern.log4j.Log4j2;

@Log4j2
public class KeyRotation {

    private KeyAgreement aliceKeyAgree;
    
    /**
     * Constructs an object for the key rotation initiator to keep and reuse 
     * the generated KeyAgreement. 
     * Note that the key rotation responder uses a static method to interact with
     * this class while the initiator needs to construct an object. 
     */ 
    public KeyRotation() {
    }

    /**
     * This is the initiator. Alice generates a public key for Bob and waits
     * for Bob's generated public key, obtained using
     * {@link #bobGenFromAlice(byte[] alicesPublicKey)},  
     * so that she can  generate the shared key herself by 
     * executing {@link #aliceFinish(byte[] bobsPublicKey)} 
     * Alice needs to reuse the KeyAgreement when finalizing the exchange
     *, which is kept in this object's memory, 
     * 
     * @return Alice's public key.
     */
    public byte[]  aliceFirst() {
        byte[] alicePubKeyEnc  = null;    
        try {
            KeyPairGenerator aliceKpairGen = KeyPairGenerator.getInstance("DH");
            aliceKpairGen.initialize(2048);// 2048 if otherside not known
            KeyPair aliceKpair = aliceKpairGen.generateKeyPair();
            // keep the agreement in memory to reuse when bob sends his public key back to alice
            aliceKeyAgree = KeyAgreement.getInstance("DH");
            aliceKeyAgree.init(aliceKpair.getPrivate());
            // encode  public key and give it to Bob.
            alicePubKeyEnc = aliceKpair.getPublic().getEncoded();
        } catch (InvalidKeyException | NoSuchAlgorithmException ex) {
            log.error(ex);
        }
        return alicePubKeyEnc;
    }


    /**
     * Uses Alice's public key generated by {@link #aliceFirst()} and obtains Bob's 
     * public key and shared secret. The public key is meant to be given to Alice
     * so that she can construct the same shared key herself. 
     * Note that this is a static method, Bob does not need to construct the object. 
     * 
     * @param alicePubKeyEnc
     * @return Pair.of(bob's public key, shared secret)
     */
    public static Pair<byte[],byte[]> bobGenFromAlice(byte[] alicePubKeyEnc) {
        byte[] bobPubKeyEnc = null;
        byte[] bobSecret  = null;
        try {
            KeyFactory bobKeyFac = KeyFactory.getInstance("DH");
            X509EncodedKeySpec x509KeySpec = new X509EncodedKeySpec(alicePubKeyEnc);
            PublicKey alicePubKey = bobKeyFac.generatePublic(x509KeySpec);
            // Bob gets the DH parameters associated with Alice's public key.
            DHParameterSpec dhParamFromAlicePubKey = ((DHPublicKey) alicePubKey).getParams();
            // Bob creates his own DH key pair
            KeyPairGenerator bobKpairGen = KeyPairGenerator.getInstance("DH");
            bobKpairGen.initialize(dhParamFromAlicePubKey);
            KeyPair bobKpair = bobKpairGen.generateKeyPair();
            // Bob creates and initializes his DH KeyAgreement object
            KeyAgreement bobKeyAgree = KeyAgreement.getInstance("DH");
            bobKeyAgree.init(bobKpair.getPrivate());
            bobPubKeyEnc = bobKpair.getPublic().getEncoded();
            bobKeyAgree.doPhase(alicePubKey, true);
            bobSecret = bobKeyAgree.generateSecret();
        } catch (NoSuchAlgorithmException | InvalidKeySpecException | InvalidAlgorithmParameterException | InvalidKeyException ex) {
            log.error(ex);
        }
        return Pair.of(bobPubKeyEnc, bobSecret);

    }

    /**
     * Use Bob's public key and the KeyAgreement from this object to generate
     * a shared secret for Alice
     * 
     * @param bobPubKeyEnc This is Bob's public key
     * @return the shared secret. 
     */
    public byte[] aliceFinish(byte[] bobPubKeyEnc) {
        if(this.aliceKeyAgree == null) throw new IllegalStateException("You must initialize the object using 'aliceFirst()'");
        try {
            KeyFactory aliceKeyFac = KeyFactory.getInstance("DH");
            X509EncodedKeySpec x509KeySpec = new X509EncodedKeySpec(bobPubKeyEnc);
            PublicKey bobPubKey = aliceKeyFac.generatePublic(x509KeySpec);
            aliceKeyAgree.doPhase(bobPubKey, true);
            
        } catch (InvalidKeySpecException | InvalidKeyException | IllegalStateException | NoSuchAlgorithmException ex) {
            log.error(ex);
        }
        return aliceKeyAgree.generateSecret();
    }
   
   
 }
