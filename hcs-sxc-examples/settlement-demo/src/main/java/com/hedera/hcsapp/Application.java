package com.hedera.hcsapp;

import com.hedera.hcsapp.appconfig.AppClient;
import com.hedera.hcsapp.entities.AddressBook;
import com.hedera.hcsapp.repository.AddressBookRepository;
import com.hedera.hcsapp.repository.CreditRepository;
import java.util.Collections;
import javax.annotation.Resource;
import org.apache.coyote.http2.Http2Protocol;

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

//            System.out.println("Let's inspect the beans provided by Spring Boot:");
//
//            String[] beanNames = ctx.getBeanDefinitionNames();
//            Arrays.sort(beanNames);
//            for (String beanName : beanNames) {
//                System.out.println(beanName);
//            }
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
                addressBookRepository.save(addressBook);
            }
        };
    }    
    
}       
