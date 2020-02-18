package com.hedera.hcsapp.entities;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import com.hedera.hcsapp.entities.AddressBook;

public class AddressBookTest {
    
    @Test
    public void testAddressBook() {
        AddressBook addressBook = new AddressBook();
        addressBook.setAppId("appid");
        addressBook.setColor("color");
        addressBook.setName("name");
        addressBook.setPaymentAccountDetails("paymentAccountDetails");
        addressBook.setPort(10);
        addressBook.setPublicKey("publicKey");
        addressBook.setRoles("roles");
        
        assertEquals("appid", addressBook.getAppId());
        assertEquals("color", addressBook.getColor());
        assertEquals("name", addressBook.getName());
        assertEquals("paymentAccountDetails", addressBook.getPaymentAccountDetails());
        assertEquals(10, addressBook.getPort());
        assertEquals("publicKey", addressBook.getPublicKey());
        assertEquals("roles", addressBook.getRoles());
    }
}
