package com.hedera.hcsapp;

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

import java.security.KeyPair;
import java.util.Arrays;
import java.util.List;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import com.hedera.hashgraph.sdk.crypto.ed25519.Ed25519PrivateKey;
import com.hedera.hcs.sxc.plugin.cryptography.StringUtils;
import com.hedera.hcs.sxc.plugin.cryptography.cryptography.Cryptography;

import io.github.cdimascio.dotenv.Dotenv;
public class GenerateConfigurationFiles {

    public static void main(String[] args) throws Exception {
        Dotenv dotenv = Dotenv.configure().filename("./config/.env").load();
        String operatorKey = dotenv.get("OPERATOR_KEY");
        String operatorId = dotenv.get("OPERATOR_ID");
        
        Ed25519PrivateKey[] playerPrivateKeys = new Ed25519PrivateKey[3];
        
        // generate the .env files for each app participant
        for (int i=0; i < 3; i++) {
            File pathToFile = new File("./config/.envPlayer-" + i);
            pathToFile.delete();
            playerPrivateKeys[i] = Ed25519PrivateKey.generate();
            List<String> lines = Arrays.asList(
                    "OPERATOR_KEY="+operatorKey
                    , "OPERATOR_ID="+operatorId
                    ,"SIGNING_KEY="+playerPrivateKeys[i].toString()
            );

            Path file = Paths.get(pathToFile.getCanonicalPath());
            Files.write(file, lines, StandardCharsets.UTF_8);
        }

        KeyPair kp = Cryptography.generateRsaKeyPair();
        byte[] secretKey =  kp.getPrivate().getEncoded();
        String P0P1 = StringUtils.byteArrayToHexString(secretKey);
        kp = Cryptography.generateRsaKeyPair();
        secretKey =  kp.getPrivate().getEncoded();
        String P0P2 = StringUtils.byteArrayToHexString(secretKey);
        
        File pathToFile = new File("./config/contact-list.yaml");
        pathToFile.delete();
        List<String> lines = Arrays.asList(
                "Player-0 :"
                ,"  Player-1 :"
                ,"    sharedSymmetricEncryptionKey: " + P0P1
                ,"    theirEd25519PubKeyForSigning: " + playerPrivateKeys[1].publicKey.toString()
                ,"  Player-2 :"
                ,"    sharedSymmetricEncryptionKey: " + P0P2
                ,"    theirEd25519PubKeyForSigning: " + playerPrivateKeys[2].publicKey.toString()
                ,"Player-1 :"
                ,"  Player-0 :"
                ,"    sharedSymmetricEncryptionKey: " + P0P1
                ,"    theirEd25519PubKeyForSigning: " + playerPrivateKeys[0].publicKey.toString()
                ,"Player-2 :"
                ,"  Player-0 :"
                ,"    sharedSymmetricEncryptionKey: " + P0P2
                ,"    theirEd25519PubKeyForSigning: " + playerPrivateKeys[0].publicKey.toString()
        );

        Path file = Paths.get(pathToFile.getCanonicalPath());
        Files.write(file, lines, StandardCharsets.UTF_8);

        System.out.println("Files generated in ./config");
    }
}
