package com.hedera.hcs.sxc.hashing;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import lombok.extern.log4j.Log4j2;

@Log4j2
public class Hashing {

   //solutionSDK.hashMessage
   public static byte[] sha(String plaintext){
       byte[] encodedhash = null; 
       try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            encodedhash = digest.digest(
                    plaintext.getBytes(StandardCharsets.UTF_8));
            return encodedhash;
        } catch (NoSuchAlgorithmException ex) {
            log.error(ex);
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
