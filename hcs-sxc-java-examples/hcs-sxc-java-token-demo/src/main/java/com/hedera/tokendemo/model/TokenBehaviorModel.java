package com.hedera.tokendemo.model;

import com.hedera.tokendemo.domain.Behavior;
import com.hedera.tokendemo.domain.Token;
import com.hedera.tokendemo.domain.TokenBehavior;
import com.hedera.tokendemo.domain.TokenTemplateBehavior;

import java.util.List;

public interface TokenBehaviorModel {

    TokenBehavior create(Token token, TokenTemplateBehavior tokenTemplateBehavior);

    boolean isMintable(Token token);

    boolean isDivisible(Token token);
    
    boolean isTransferable(Token token);

    List<Behavior> findTokenBehaviors(Long tokenId);

    boolean isBurnable(Token token);
}