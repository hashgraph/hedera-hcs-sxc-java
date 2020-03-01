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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

import org.yaml.snakeyaml.Yaml;

import lombok.extern.log4j.Log4j2;

/**
 * Manages configuration
 */
@Log4j2
public enum AddressListCrypto {
    INSTANCE();
    
    private boolean isInitialised = false;
    private Map<String,  // Bob | Carlos ...
            Map<String,  // sharedSymmetricEncryptionKey | theirEd25519PubKeyForSigning
                String>
            > addressList;
    
    
    AddressListCrypto() throws ExceptionInInitializerError{
    }
    
    public AddressListCrypto singletonInstance(String appId) throws Exception{
        if( ! this.isInitialised) {
            this.init("./config/contact-list.yaml", appId);
        }
        return INSTANCE;
    }
    
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
    
    public  Map<String, Map<String, String>> getAddressList(){
        return this.addressList;
    }
    
  

 
}
