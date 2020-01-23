package com.hedera.hcsapp.controllers;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.hedera.hcsapp.AppData;
import com.hedera.hcsapp.dockercomposereader.DockerCompose;
import com.hedera.hcsapp.dockercomposereader.DockerComposeReader;
import com.hedera.hcsapp.dockercomposereader.DockerService;
import com.hedera.hcsapp.entities.AddressBook;
import com.hedera.hcsapp.repository.AddressBookRepository;
import com.hedera.hcsapp.restclasses.AddressBookRest;
import lombok.extern.log4j.Log4j2;

@Log4j2
@RestController
public class AddressBookController {

    private static AppData appData;
    
    public AddressBookController() throws Exception {
        appData = new AppData();
    }

    @Autowired
    AddressBookRepository addressBookRepository;

    @GetMapping(value = "/addressbook/buyerseller", produces = "application/json")
    public List<AddressBook> addressbookBuyerSeller() throws FileNotFoundException, IOException {
//        AppData appData = new AppData();
        log.debug("/addressbook/buyerorseller");
        return addressBookRepository.findAllWithRoleButMe(appData.getUserName(),"BUYER,SELLER");
    }

    @GetMapping(value = "/addressbook/appusers", produces = "application/json")
    public ResponseEntity<List<AddressBookRest>> addressbookAppUsers() throws Exception {
        log.debug("/addressbook/appusers");
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type", "application/json");

        List<AddressBookRest> restResponse = new ArrayList<AddressBookRest>();
        
        DockerCompose dockerCompose = DockerComposeReader.parse();
        
        for (Map.Entry<String, DockerService> dockerService : dockerCompose.getServices().entrySet()) {
            DockerService service = dockerService.getValue();
            if ((null != service.getEnvironment()) && (service.getEnvironment().containsKey("APP_ID"))) {
                
                String name = service.getContainer_name();
                String publicKey = service.getEnvironment().get("PUBKEY");
                String roles = service.getEnvironment().get("ROLES");
                String paymentAccountDetails = service.getEnvironment().get("PAYMENT_ACCOUNT_DETAILS");
                String color = service.getEnvironment().get("COLOR");
                long port = Long.parseLong(service.getPort());
                long appId = Long.parseLong(service.getEnvironment().get("APP_ID"));
                
                restResponse.add(new AddressBookRest(name, publicKey, roles, paymentAccountDetails, port, color, appId));
            }
            
        }
        
//        List<AddressBook> addressBookList = new ArrayList<AddressBook>();
//
//        addressBookList = addressBookRepository.findAllUsers();
//        
//        for (AddressBook addressBook : addressBookList) {
//            restResponse.add(new AddressBookRest(addressBook, index));
//            index += 1;
//        }
//
        return new ResponseEntity<>(restResponse, headers, HttpStatus.OK);
    }

    @GetMapping(value = "/addressbook/paychannel", produces = "application/json")
    public List<AddressBook> addressbookPaychannel() throws FileNotFoundException, IOException {
        log.debug("/addressbook/paychannel");
        return addressBookRepository.findAllWithRoleButMe(appData.getUserName(),"PAYCHANNEL");
    }
    
    @GetMapping(value = "/addressbook-everything", produces = "application/json")
    public List<AddressBook> addressbookEverything() throws FileNotFoundException, IOException {
//        AppData appData = new AppData();
        return addressBookRepository.findAllUsers();
    }
    
    @GetMapping(value = "/addressbook", produces = "application/json")
    public List<AddressBook> addressbookAll() throws FileNotFoundException, IOException {
//        AppData appData = new AppData();
        log.debug("/addressbook");
        return addressBookRepository.findAllUsersButMe(appData.getUserName());
    }
    @GetMapping(value = "/addressbook/me", produces = "application/json")
    public AddressBook addressbookMe() throws FileNotFoundException, IOException {
        log.debug("/addressbook");
        return addressBookRepository.findById(appData.getUserName()).get();
    }
}
