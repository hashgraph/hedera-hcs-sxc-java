package com.hedera.tokendemo.domain;

import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.Table;

@Data
@Entity
@IdClass(TokenTemplateBehaviorId.class)
@Table(name = "token_template_behaviors")
public class TokenTemplateBehavior {
    @Id
    private Long tokenTemplateId;
    @Id
    private Long behaviorId;
}
