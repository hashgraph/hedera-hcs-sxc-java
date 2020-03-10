package com.hedera.hcs.sxc.hashing;

/*-
 * ‌
 * hcs-sxc-java
 * ​
 * Copyright (C) 2019 - 2020 Hedera Hashgraph, LLC
 * ​
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ‍
 */

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

public class Hashing {

   public static byte[] sha(String plaintext) throws NoSuchAlgorithmException{
       byte[] encodedhash = null; 
        MessageDigest digest = MessageDigest.getInstance("SHA-384");
        encodedhash = digest.digest(
                plaintext.getBytes(StandardCharsets.UTF_8));
        return encodedhash;
    }
   
   public static boolean matchSHA(byte[] sha1, byte[] sha2){
       return Arrays.equals(sha1, sha2);
   }
   
   public static boolean verifySHA(byte[] sha, String plaintext) throws NoSuchAlgorithmException{
        return Arrays.equals(sha(plaintext), sha);
   }

}
