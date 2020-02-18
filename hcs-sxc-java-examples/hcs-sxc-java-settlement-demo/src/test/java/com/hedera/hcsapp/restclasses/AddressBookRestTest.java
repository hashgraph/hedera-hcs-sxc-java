package com.hedera.hcsapp.restclasses;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

public class AddressBookRestTest {
    
    @Test
    public void testAddressBookRest() {
        AddressBookRest addressBookRest = new AddressBookRest("alice", "publicKey", "roles", "paymentAccountDetails", 8080, "color", "5");
        assertEquals("alice", addressBookRest.getName());
        assertEquals("publicKey", addressBookRest.getPublicKey());
        assertEquals("roles", addressBookRest.getRoles());
        assertEquals("paymentAccountDetails", addressBookRest.getPaymentAccountDetails());
        assertEquals(8080, addressBookRest.getPort());
        assertEquals("color", addressBookRest.getColor());
        assertEquals("5", addressBookRest.getAppId());
    }
}
