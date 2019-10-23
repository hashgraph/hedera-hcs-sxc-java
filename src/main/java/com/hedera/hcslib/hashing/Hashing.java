package com.hedera.hcslib.hashing;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

import java.security.NoSuchAlgorithmException;
import java.util.Arrays;


import java.util.logging.Level;
import java.util.logging.Logger;

public class Hashing {

   //solutionSDK.hashMessage
   public static byte[] sha(String plaintext){
       byte[] encodedhash = null; 
       try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            encodedhash = digest.digest(
                    plaintext.getBytes(StandardCharsets.ISO_8859_1));
            return encodedhash;
        } catch (NoSuchAlgorithmException ex) {
            Logger.getLogger(Hashing.class.getName()).log(Level.SEVERE, null, ex);
        }
        return encodedhash;
    }
   
   public static boolean matchSHA(byte[] sha1, byte[] sha2){
       return Arrays.equals(sha1, sha2);
   }
   
   public static boolean verifySHA(byte[] sha, String plaintext){
        return Arrays.equals(sha(plaintext), sha);
   }

}
