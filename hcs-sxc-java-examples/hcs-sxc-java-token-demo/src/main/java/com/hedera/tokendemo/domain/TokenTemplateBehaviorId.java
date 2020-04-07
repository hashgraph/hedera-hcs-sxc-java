package com.hedera.tokendemo.domain;

import lombok.Data;

import java.io.Serializable;

@Data
public class TokenTemplateBehaviorId implements Serializable {
    static final long serialVersionUID = 1L;
    private Long tokenTemplateId;
    private Long behaviorId;
 
    public TokenTemplateBehaviorId() {
        
    }
    public TokenTemplateBehaviorId(long tokenTemplateId, long behaviorId) {
        this.tokenTemplateId = tokenTemplateId;
        this.behaviorId = behaviorId;
    }
}