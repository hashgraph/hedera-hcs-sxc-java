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

import com.hedera.hcs.sxc.HCSCore;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

import org.yaml.snakeyaml.Yaml;

import lombok.extern.log4j.Log4j2;

/**
    * Singleton object to hold an address-book with details for communicating parties. 
    * Participants
    * are related by sharing secret keys; each related participant has a different
    * shared key. The address list also contains the public signing key of each
    * related participant, which is used to identify users and to verify messages.
    * The file allows the creation of multiple address-books in a single
    * file. A mapping from appId to address-list is used to select the active addressbook.
    * Use {@link #singletonInstance(java.lang.String)} to load an address book at the 
    * default location and {@link #getAddressList()} to get the map containing app-net
    * participant details. 
    * 
    * Note: in order to link the address book with hcs-core, the core needs to be
    * supplied with address book details using {@link  HCSCore.addOrUpdateAppParticipant}.
    * A convenience function is implemented in {@link #supplyCore(com.hedera.hcs.sxc.HCSCore) }
    * 
 */
@Log4j2
public enum AddressListCrypto {
    INSTANCE();
    
    private boolean isInitialised = false;
    
    /** A mapping of the form:
     * * <pre>
     * {  app-id-1:
     *       { theirEd25519PubKeyForSigning:302A493..9E
     *       , sharedSymmetricEncryptionKey:4837AE..F
     *       }
     *    , 
     *    ... 
     * ,  app-id-n:
     *       { theirEd25519PubKeyForSigning:302B493..9F
     *        ,sharedSymmetricEncryptionKey:6837AE..E
     *       }
     * }
     * </pre>
     */
    private Map<String,  // Bob | Carlos ...
            Map<String,  // sharedSymmetricEncryptionKey | theirEd25519PubKeyForSigning
                String>
            > addressList;
    
    
    AddressListCrypto() throws ExceptionInInitializerError{
    }
    
    /**
     * Creates an address book instance from a file located in "./config/contact-list.yaml" 
     * to hold related app-net participants. Participants
     * are related by sharing secret keys; each related participant has a different
     * shared key. The address list also contains the public signing key of each
     * related participant, which is used to identify users and to verify messages.
     * The file allows the creation of multiple address books in a single
     * file. A mapping from appId to address-list is used to select the active address book.
     * 
     * @param appId selects the active address book in the file
     * @return singleton instance of the address list. 
     * @throws Exception 
     */
    public AddressListCrypto singletonInstance(String appId) throws Exception{
        if( ! this.isInitialised) {
            this.init("./config/contact-list.yaml", appId);
        }
        return INSTANCE;
    }
    
    /**
     * Overrides default addressbook file location
     * @see {@link #singletonInstance} 
     * @param addressFilePath
     * @param appId
     * @return
     * @throws Exception 
     */
    public AddressListCrypto singletonInstance(String addressFilePath, String appId) throws Exception{
        if( ! this.isInitialised) {
            this.init(addressFilePath, appId);
        }
        return INSTANCE;
    }
    
    // Constructor for testing
    private void init (String configFilePath, String appId) throws FileNotFoundException, IOException {
        
        Yaml yaml = new Yaml();
        File configFile = new File(configFilePath);
        if (configFile.exists()) {
            log.debug("Loading contact-list.yaml from " + configFilePath);
            InputStream inputStream = new FileInputStream(configFile.getCanonicalPath());
            Map<String, Object> obj = yaml.load(inputStream);
            addressList = (Map<String, Map<String, String>>) obj.get(appId);
            if (addressList == null) {
                System.out.println("Unable to locate player in contact list, check your app id is either Player-0, Player-1 or Player-2. I have: " + appId);
                System.exit(0);
            }
            this.isInitialised = true;
           
        } else {
            log.error("Unable to find file " + configFilePath);
            System.exit(0);
        }
    }
    
    /**
     * Returns participant details. The map has the following structure
     * <pre>
     * {  app-id-1:
     *       { theirEd25519PubKeyForSigning:3023339..9E
     *       , sharedSymmetricEncryptionKey:4837AE..F
     *       }
     *    , 
     *    ... 
     * ,  app-id-n:
     *       { theirEd25519PubKeyForSigning:302A693..97
     *        ,sharedSymmetricEncryptionKey:4837AF..E
     *       }
     * }
     * </pre>
     * @return the mapping.  
     */
    public  Map<String, Map<String, String>> getAddressList(){
        return this.addressList;
    }

    /**
     * Links this address book to HCSCore by feeding this {@link #addressList} to it.
     * @param hcsCore 
     */
    void supplyCore(HCSCore hcsCore) {
        this
        .getAddressList()
        .forEach((k,v)->{
            hcsCore.addOrUpdateAppParticipant(k, v.get("theirEd25519PubKeyForSigning"), v.get("sharedSymmetricEncryptionKey"));
        });
    }
    
  

 
}
