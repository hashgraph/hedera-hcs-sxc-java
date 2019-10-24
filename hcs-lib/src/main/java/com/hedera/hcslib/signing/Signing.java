package com.hedera.hcslib.signing;

import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;

import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.security.SignatureException;


import java.util.logging.Level;
import java.util.logging.Logger;

public class Signing {
 
    //solutionSDK.signMessage
    public static byte[] sign(byte[] payload , PrivateKey privateKey) throws Exception {
        Signature privateSignature = Signature.getInstance("SHA256withRSA");
        privateSignature.initSign(privateKey);
        privateSignature.update(payload);
        byte[] signature = privateSignature.sign();
        return signature;
    }
    
    //solutionSDK.verify    
    public static boolean verify(String plainText, byte[] signature, PublicKey publicKey)  {
        boolean b = false;
        try {
            Signature publicSignature = Signature.getInstance("SHA256withRSA");
            publicSignature.initVerify(publicKey);
            publicSignature.update(plainText.getBytes(StandardCharsets.ISO_8859_1));
            b = publicSignature.verify(signature);
        } catch (InvalidKeyException | SignatureException | NoSuchAlgorithmException ex) {
            Logger.getLogger(Signing.class.getName()).log(Level.SEVERE, null, ex);
        }
        return b;
    }
    
 }
