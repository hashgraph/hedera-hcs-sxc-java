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

import com.hedera.hcsapp.appconfig.AppClient;
import com.hedera.hcsapp.entities.AddressBook;
import com.hedera.hcsapp.repository.AddressBookRepository;

import java.util.Collections;
import java.util.Map;

import javax.annotation.Resource;
import javax.sql.DataSource;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

@SpringBootApplication
public class Application {

    @Resource
    AddressBookRepository addressBookRepository;
    
    public static void main(String[] args) throws Exception {

        AppData appData = new AppData();   
        SpringApplication app = new SpringApplication(Application.class);
        //app.setDefaultProperties(Collections
        //  .singletonMap("server.port", appData.getWebPort()));
        app.run(args);
    }
    
    @Bean
    public CommandLineRunner commandLineRunner(ApplicationContext ctx) {
        return args -> {

            AppData appData = new AppData();
           
            // delete the address book
            addressBookRepository.deleteAll();
            
            // populate the address book
            for (AppClient appClient : appData.getAppClients()) {   
                AddressBook addressBook = new AddressBook();
                addressBook.setName(appClient.getClientName());
                addressBook.setPublicKey(appClient.getClientKey());
                addressBook.setRoles(appClient.getRoles());
                addressBook.setPaymentAccountDetails(appClient.getPaymentAccountDetails());
                addressBook.setColor(appClient.getColor());
                addressBook.setAppId(appClient.getAppId());
                addressBook.setPort(appClient.getWebPort());
                addressBookRepository.save(addressBook);
            }
        };
    }    
    
}       
