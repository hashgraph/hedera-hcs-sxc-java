package com.hedera.tokendemo.domain;

import lombok.Data;

import java.io.Serializable;

@Data
public class TokenBehaviorId implements Serializable {
    static final long serialVersionUID = 1L;
    private Long tokenId;
    private Long behaviorId;
 
    public TokenBehaviorId() {
        
    }
    public TokenBehaviorId(long tokenId, long behaviorId) {
        this.tokenId = tokenId;
        this.behaviorId = behaviorId;
    }
}