package com.hedera.tokendemo.domain;

import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

@Data
@Entity
@Table(name = "token_templates")
public class TokenTemplate {
    @Id 
    @GeneratedValue
    private Long id;
    private String name;
}
