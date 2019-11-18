package com.hedera.hcsapp.entities;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.Data;

@Data
@Entity
@Table(name = "address_book")
public final class AddressBook {

    @Id
    private String name;
    private String publicKey;
    private String roles;
}
