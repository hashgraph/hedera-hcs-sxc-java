package main.java.com.hedera.hcsapp;

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

import com.hedera.hcs.sxc.plugin.encryption.diffiehellman.Encryption;
import com.hedera.hcs.sxc.plugin.encryption.diffiehellman.StringUtils;

public class GenerateSymmetricKey {

    public static void main(String[] args) throws Exception {
        byte[] secretKey =  new Encryption().generateSecretKey();
        System.out.println("Private key : " + StringUtils.byteArrayToHexString(secretKey));
    }

}
