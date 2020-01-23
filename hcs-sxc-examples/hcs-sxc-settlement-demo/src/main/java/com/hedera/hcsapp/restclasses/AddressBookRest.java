package com.hedera.hcsapp.restclasses;

import lombok.Data;

@Data
public final class AddressBookRest {

    private String name;
    private String publicKey;
    private String roles;
    private String paymentAccountDetails;    
    private long port;
    private String color;
    private long appId;
    
    public AddressBookRest(String name, String publicKey, String roles, String paymentAccountDetails, long port, String color, long appId) {
        this.name = name;
        this.publicKey = publicKey;
        this.roles = roles;
        this.paymentAccountDetails = paymentAccountDetails;
        this.port = port;
        this.color = color;
        this.appId = appId;
    }
//    public AddressBookRest(AddressBook addressBook, int index) {
//        this.name = addressBook.getName();
//        this.publicKey = addressBook.getPublicKey();
//        this.roles = addressBook.getRoles();
//        this.paymentAccountDetails = addressBook.getPaymentAccountDetails();
//        this.port = 8081 + index;
//        this.color = addressBook.getColor();
//        this.appId = index;
//    }
}
