package com.hedera.tokendemo.domain;

import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

@Data
@Entity
@Table(name = "operations")
public class Operation {
    @Id 
    @GeneratedValue
    private long id;
    private String operator;
    private String recipient;
    private String operation;
}