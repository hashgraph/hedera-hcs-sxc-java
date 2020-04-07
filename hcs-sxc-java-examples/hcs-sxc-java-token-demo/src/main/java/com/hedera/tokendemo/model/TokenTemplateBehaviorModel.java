package com.hedera.tokendemo.model;

import com.hedera.tokendemo.domain.Token;
import com.hedera.tokendemo.domain.TokenTemplate;

public interface TokenTemplateBehaviorModel {

    boolean isMintable(TokenTemplate tokenTemplate);

    boolean isDivisible(TokenTemplate tokenTemplate);

    void copyBehaviorsToToken(long tokenTemplateId, Token token);
}