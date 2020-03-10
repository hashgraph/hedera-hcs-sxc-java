package com.hedera.hcs.sxc.signing;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import com.hedera.hashgraph.sdk.account.AccountId;
import com.hedera.hashgraph.sdk.crypto.ed25519.Ed25519PrivateKey;
import com.hedera.hashgraph.sdk.crypto.ed25519.Ed25519PublicKey;
import com.hedera.hcs.sxc.config.Topic;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;

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


class SigningTest {


    @Test
    public void testSigning() throws Exception {
        Ed25519PrivateKey privateKey = Ed25519PrivateKey.generate();
        Ed25519PublicKey fakepublicKey = Ed25519PrivateKey.generate().publicKey;
        
        String cleartext = "Hear my cries Hear 234sdf! �$%&*)_+ my call Lend me your ears See my fall See my error Know my faults Time halts See my loss ";
        byte[] signString = Signing.sign(cleartext, privateKey);
        byte[] signBytes = Signing.sign(cleartext.getBytes(), privateKey);
        assertArrayEquals(signString, signBytes);
        
        assertTrue(Signing.verify(cleartext, signBytes, privateKey.publicKey));
        assertTrue(Signing.verify(cleartext.getBytes(), signBytes, privateKey.publicKey));
        
        assertFalse(Signing.verify("not the original message", signBytes, privateKey.publicKey));

        assertFalse(Signing.verify(cleartext, signBytes, fakepublicKey));
        
       
        
        assertFalse(Signing.verify(cleartext, new byte[0], privateKey.publicKey ));
        
        
    }


}
