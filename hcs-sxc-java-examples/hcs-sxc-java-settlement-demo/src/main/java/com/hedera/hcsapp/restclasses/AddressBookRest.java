package com.hedera.hcsapp.restclasses;

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
