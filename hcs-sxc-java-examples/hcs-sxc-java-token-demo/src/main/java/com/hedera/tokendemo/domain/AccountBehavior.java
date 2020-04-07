package com.hedera.tokendemo.domain;

import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.Table;

@Data
@Entity
@IdClass(AccountBehaviorId.class)
@Table(name = "account_behaviors")
public class AccountBehavior {
    @Id
    private Long accountId;
    @Id
    private Long behaviorId;
}
