package com.hedera.tokendemo.domain;

import lombok.Data;

import java.io.Serializable;

@Data
public class UserAccountId implements Serializable {
    static final long serialVersionUID = 1L;
    private Long userId;
    private Long accountId;
 
    public UserAccountId() {
        
    }
    public UserAccountId(long userId, long accountId) {
        this.userId = userId;
        this.accountId = accountId;
    }
}
