package com.hedera.tokendemo.domain;

import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.Table;

@Data
@Entity
@IdClass(UserAccountId.class)
@Table(name = "user_accounts")
public class UserAccount {
    @Id
    private long userId;
    @Id
    private long accountId;
    private String hederaAccountId;
}
