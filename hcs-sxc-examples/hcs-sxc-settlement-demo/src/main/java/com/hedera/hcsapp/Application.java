package com.hedera.hcsapp;

import com.hedera.hcsapp.appconfig.AppClient;
import com.hedera.hcsapp.entities.AddressBook;
import com.hedera.hcsapp.repository.AddressBookRepository;


import javax.annotation.Resource;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class Application {

    @Resource
    AddressBookRepository addressBookRepository;

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
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
                addressBookRepository.save(addressBook);
            }
        };
    }    
    
}       
