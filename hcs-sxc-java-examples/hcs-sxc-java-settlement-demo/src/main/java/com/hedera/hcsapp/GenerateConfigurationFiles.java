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

import com.hedera.hashgraph.sdk.crypto.ed25519.Ed25519PrivateKey;
import com.hedera.hcs.sxc.plugin.encryption.diffiehellman.Encryption;
import com.hedera.hcs.sxc.plugin.encryption.diffiehellman.StringUtils;

import java.io.File;

import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import org.yaml.snakeyaml.Yaml;

public class GenerateConfigurationFiles {

    public static void main(String[] args) throws Exception {
        generateConfig();
    }
    public static void generateConfig() throws Exception {
        // load the composer template that contains all participants
        Yaml dockerComposeYaml = new Yaml();
        String dockerComposeFile = new File("./config/docker-compose.yml").getCanonicalPath();
        File dockerComposeInput = new File(dockerComposeFile);
        InputStream dockerComposeInputStream = new FileInputStream(dockerComposeInput);
        Map<String, Map<String, Map<String, Map<String, String>>>> dockComposeTemplate = dockerComposeYaml
                .load(dockerComposeInputStream);

        Map<String, Map<String, Map<String, String>>> addressTemplate = new HashMap<String, Map<String, Map<String, String>>>();

        for (String appUser : dockComposeTemplate.get("services").keySet()) {
            String appUserRoles = dockComposeTemplate.get("services").get(appUser).get("environment").get("ROLES");

            for (String otherParty : dockComposeTemplate.get("services").keySet()) {
                String otherPartyRoles = dockComposeTemplate.get("services").get(otherParty).get("environment")
                        .get("ROLES");

                if (!appUser.contentEquals(otherParty)) {
                    switch (appUserRoles) {
                    case "PAYCHANNEL":
                        // paychannels exchange with everyone except pay channels
                        if (!otherPartyRoles.contentEquals("PAYCHANNEL")) {
                            if (addressTemplate.get(appUser) == null) {
                                addressTemplate.put(appUser,new HashMap<String, Map<String, String>>());
                            }
                            addressTemplate.get(appUser).put(otherParty, new HashMap<String, String>());
                        }
                        break;
                    case "BUYER,SELLER":
                        // buyer/seller exchange with everyone
                    case "AUDITOR":
                        // Demo receive from everyone
                        if (addressTemplate.get(appUser) == null) {
                            addressTemplate.put(appUser,new HashMap<String, Map<String, String>>());
                        }
                        addressTemplate.get(appUser).put(otherParty, new HashMap<String, String>());
                        break;
                    case "ARBITER":
                        // Arbiter/auditor - not involved yet
                    }
                }
            }
        }

        for (String player : dockComposeTemplate.get("services").keySet()) {
            Map<String, String> env = dockComposeTemplate.get("services").get(player).get("environment");
            Ed25519PrivateKey key = Ed25519PrivateKey.generate();
            env.put("PUBKEY", key.publicKey.toString());
            env.put("SIGNKEY", key.toString());

            // populate all public keys and init mutable map
            for (String playerInAddress : addressTemplate.keySet()) {
                if (addressTemplate.get(playerInAddress).containsKey(player)) {
                    if (addressTemplate.get(playerInAddress).get(player) == null) {
                        Map<String, String> m = new HashMap<>();
                        m.put("theirEd25519PubKeyForSigning", key.publicKey.toString());
                        addressTemplate.get(playerInAddress).put(player, m);
                    } else {
                        addressTemplate.get(playerInAddress).get(player).put("theirEd25519PubKeyForSigning",
                                key.publicKey.toString());
                    }
                }
            }

            // set pair wise shared keys
            for (String playerInAddress : addressTemplate.keySet()) {
                if (addressTemplate.get(playerInAddress).containsKey(player)) {
                    String sharedKey = StringUtils
                            .byteArrayToHexString(new Encryption().generateSecretKey());

                    addressTemplate.get(playerInAddress).get(player).put("sharedSymmetricEncryptionKey", sharedKey);
                    if (addressTemplate.get(player) != null) {
                        if (addressTemplate.get(player).get(playerInAddress) != null) {
                            addressTemplate.get(player).get(playerInAddress).put("sharedSymmetricEncryptionKey",
                                    sharedKey);
                        }

                    }
                }
            }
        }
        Yaml dockComposeOut = new Yaml();
        FileWriter dockComposeWriter = new FileWriter(dockerComposeFile);
        dockComposeWriter.write(dockComposeOut.dumpAsMap(dockComposeTemplate));
        dockComposeWriter.close();

        Yaml addressOut = new Yaml();
        String contactListFile = new File("./config/contact-list.yaml").getCanonicalPath();
        FileWriter addressOutWriter = new FileWriter(contactListFile);
        addressOutWriter.write(dockComposeOut.dumpAsMap(addressTemplate));
        addressOutWriter.close();

        System.out.println("Configuration file generation complete.");
    }
}
