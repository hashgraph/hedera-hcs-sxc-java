package com.hedera.hcs.sxc.signing;

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

import org.bouncycastle.math.ec.rfc8032.Ed25519;
import com.hedera.hashgraph.sdk.crypto.ed25519.Ed25519PrivateKey;
import com.hedera.hashgraph.sdk.crypto.ed25519.Ed25519PublicKey;

import lombok.extern.log4j.Log4j2;

@Log4j2
public class Signing {
 
    public static byte[] sign(String messageToSign, Ed25519PrivateKey privateKey) throws Exception {
        return sign(messageToSign.getBytes(), privateKey);
    }

    public static byte[] sign(byte[] messageToSign, Ed25519PrivateKey privateKey) throws Exception {
        return privateKey.sign(messageToSign);
    }
    
    public static boolean verify(String plainText, byte[] signature, Ed25519PublicKey publicKey)  {
        return verify(plainText.getBytes(), signature, publicKey);
    }
    public static boolean verify(byte[] plainText, byte[] signature, Ed25519PublicKey publicKey)  {
        return Ed25519.verify(signature, 0, publicKey.toBytes(), 0, plainText, 0, plainText.length);
    }        
 }
