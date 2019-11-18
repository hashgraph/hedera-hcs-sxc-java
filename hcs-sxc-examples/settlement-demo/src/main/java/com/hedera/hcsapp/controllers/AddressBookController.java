package com.hedera.hcsapp.controllers;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.hedera.hcsapp.AppData;
import com.hedera.hcsapp.entities.AddressBook;
import com.hedera.hcsapp.repository.AddressBookRepository;
import lombok.extern.log4j.Log4j2;

@Log4j2
@Transactional
@RestController
public class AddressBookController {

    @Autowired
    AddressBookRepository addressBookRepository;

    @GetMapping(value = "/addressbook/buyerseller", produces = "application/json")
    public List<AddressBook> addressbookBuyerSeller() throws FileNotFoundException, IOException {
        AppData appData = new AppData();
        log.debug("/addressbook/buyerorseller");
        return addressBookRepository.findAllBuyersSellersButMe(appData.getUserName(),"BUYER,SELLER");
    }
    @GetMapping(value = "/addressbook", produces = "application/json")
    public List<AddressBook> addressbookAll() throws FileNotFoundException, IOException {
        AppData appData = new AppData();
        log.debug("/addressbook");
        return addressBookRepository.findAllUsersButMe(appData.getUserName());
    }
    @GetMapping(value = "/addressbook/me", produces = "application/json")
    public AddressBook addressbookMe() throws FileNotFoundException, IOException {
        AppData appData = new AppData();
        log.debug("/addressbook");
        return addressBookRepository.findById(appData.getUserName()).get();
    }
}
