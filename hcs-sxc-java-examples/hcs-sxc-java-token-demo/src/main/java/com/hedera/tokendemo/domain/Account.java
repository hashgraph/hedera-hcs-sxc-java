package com.hedera.tokendemo.domain;

import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

@Data
@Entity
@Table(name = "accounts")
public class Account {
    @Id 
    @GeneratedValue
    private long id;
    private String keys;
    private long tokenId;
    private long balance;
}
