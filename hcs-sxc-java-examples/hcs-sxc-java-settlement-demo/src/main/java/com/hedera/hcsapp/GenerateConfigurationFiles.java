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

import com.google.common.collect.HashBiMap;
import com.hedera.hashgraph.sdk.crypto.ed25519.Ed25519PrivateKey;
import com.hedera.hcs.sxc.plugin.cryptography.StringUtils;
import com.hedera.hcs.sxc.plugin.cryptography.cryptography.Cryptography;
import java.io.File;


import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import org.yaml.snakeyaml.Yaml;
public class GenerateConfigurationFiles {

    public static void main(String[] args) throws Exception {
        
        // load the composer template that contains all participants
        Yaml dockerComposeYaml = new Yaml();
        File dockerComposeInput = new File("./config/docker-compose.yml.sample");
        InputStream dockerComposeInputStream = new FileInputStream(dockerComposeInput);
        Map<String,
           Map<String,
             Map<String,
                Map<String,String>
        >>> dockComposeTemplate = dockerComposeYaml.load(dockerComposeInputStream);
        
        // loast all address list template that contains relationships between participants
        Yaml addressYaml = new Yaml();
        File addressInput = new File("./config/contact-list.template.yaml");
        InputStream addressInputStream = new FileInputStream(addressInput);
        Map<String,
           Map<String,
             Map<String,String>
        >> addressTemplate = addressYaml.load(addressInputStream);
        
        
       
        for (String player :  dockComposeTemplate.get("services").keySet()){
            Map<String, String> env = dockComposeTemplate
                    .get("services")
                    .get(player)
                    .get("environment");
                    Ed25519PrivateKey key = Ed25519PrivateKey.generate();            
                    env.put("PUBKEY", key.publicKey.toString());
                    env.put("SIGNKEY", key.toString());
                    
                    // pobulate all public keys and init mutable map
                    for (String playerInAddress : addressTemplate.keySet()){
                        if (addressTemplate.get(playerInAddress).containsKey(player)){
                            if (addressTemplate.get(playerInAddress).get(player) == null){
                                Map<String,String> m = new HashMap<>();
                                m.put("theirEd25519PubKeyForSigning", key.publicKey.toString());
                                addressTemplate.get(playerInAddress)
                                        .put(player, m)
                                ;
                            } else {
                                addressTemplate.get(playerInAddress)
                                .get(player).put(
                                        "theirEd25519PubKeyForSigning", key.publicKey.toString()
                                ) ;
                            }
                        }
                     }
                    
                     // set pair wise shared keys
                    for (String playerInAddress : addressTemplate.keySet()){
                        if (addressTemplate.get(playerInAddress).containsKey(player)){
                            //if (addressTemplate.get(playerInAddress).get(player) == null){
                                //Map<String, String> p = addressTemplate.get(playerInAddress).get(player);
                                String sharedKey = StringUtils.byteArrayToHexString( Cryptography.generateRsaKeyPair().getPrivate().getEncoded());
                                //Map<String,String> m2 = new HashMap<>();
                                
                                addressTemplate.get(playerInAddress)
                                        .get(player)
                                        .put("sharedSymmetricEncryptionKey", sharedKey);
                                //Map<String,String> m3 = new HashMap<>();
                                if(addressTemplate.get(player)
                                        .get(playerInAddress) !=null ){
                                addressTemplate.get(player)
                                        .get(playerInAddress)
                                        .put("sharedSymmetricEncryptionKey", sharedKey)
                                ;
                                }
                            }    
                        //}   
                        
                   
                    }
                    
        }
        Yaml dockComposeOut = new Yaml();
        FileWriter dockComposeWriter = new FileWriter("./config/docker-compose.yml");
        dockComposeWriter.write(dockComposeOut.dumpAsMap(dockComposeTemplate));
        dockComposeWriter.close();
                
        Yaml addressOut = new Yaml();
        FileWriter addressOutWriter = new FileWriter("./config/contact-list.yaml");
        addressOutWriter.write(dockComposeOut.dumpAsMap(addressTemplate));
        addressOutWriter.close(); 
    }
}
