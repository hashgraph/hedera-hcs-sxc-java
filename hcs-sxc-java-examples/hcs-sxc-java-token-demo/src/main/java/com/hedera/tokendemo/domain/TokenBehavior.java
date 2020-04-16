package com.hedera.tokendemo.domain;

import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.Table;

@Data
@Entity
@IdClass(TokenBehaviorId.class)

@Table(name = "token_behaviors")
public class TokenBehavior {
    @Id
    private Long tokenId;
    @Id
    private Long behaviorId;
}
