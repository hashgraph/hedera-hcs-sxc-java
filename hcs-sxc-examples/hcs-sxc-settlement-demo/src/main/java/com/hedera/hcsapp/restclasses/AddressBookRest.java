package com.hedera.hcsapp.restclasses;

import com.hedera.hcsapp.entities.AddressBook;
import lombok.Data;

@Data
public final class AddressBookRest {

    private String name;
    private String publicKey;
    private String roles;
    private String paymentAccountDetails;    
    private int port;
    
    public AddressBookRest(String name, String publicKey, String roles, String paymentAccountDetails, int port) {
        this.name = name;
        this.publicKey = publicKey;
        this.roles = roles;
        this.paymentAccountDetails = paymentAccountDetails;
        this.port = port;
    }
    public AddressBookRest(AddressBook addressBook, int index) {
        this.name = addressBook.getName();
        this.publicKey = addressBook.getPublicKey();
        this.roles = addressBook.getRoles();
        this.paymentAccountDetails = addressBook.getPaymentAccountDetails();
        this.port = 8081 + index;
    }
}
