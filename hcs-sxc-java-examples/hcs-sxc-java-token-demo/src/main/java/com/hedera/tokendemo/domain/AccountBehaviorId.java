package com.hedera.tokendemo.domain;

import lombok.Data;

import java.io.Serializable;

@Data
public class AccountBehaviorId implements Serializable {
    static final long serialVersionUID = 1L;
    private Long accountId;
    private Long behaviorId;
 
    public AccountBehaviorId() {
        
    }
    public AccountBehaviorId(long accountId, long behaviorId) {
        this.accountId = accountId;
        this.behaviorId = behaviorId;
    }
}