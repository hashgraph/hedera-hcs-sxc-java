package com.hedera.tokendemo.domain;

import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

@Data
@Entity
@Table(name = "tokens")
public class Token {
    @Id 
    @GeneratedValue
    private Long id;
    private Long ownerUserId;
    private String name;
    private String symbol;
    private int decimals;
    private long quantity;
    private Long cap;
    private Long balance;
    
    public Token() {
        cap = -1L;
        quantity = 0L;
        decimals = 0;
        balance = 0L;
    }
}