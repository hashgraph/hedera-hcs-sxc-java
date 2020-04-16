package com.hedera.tokendemo.service;

import com.hedera.tokendemo.domain.Behavior;
import com.hedera.tokendemo.domain.Token;
import com.hedera.tokendemo.domain.TokenBehavior;
import com.hedera.tokendemo.domain.TokenTemplateBehavior;
import com.hedera.tokendemo.model.TokenBehaviorModel;
import com.hedera.tokendemo.repository.TokenBehaviorRepository;
import com.hedera.tokendemo.utils.Behaviors;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TokenBehaviorService implements TokenBehaviorModel {
    private final TokenBehaviorRepository tokenBehaviorRepository;

    public TokenBehaviorService(TokenBehaviorRepository tokenBehaviorRepository) {
        this.tokenBehaviorRepository = tokenBehaviorRepository;
    }

    @Override
    public TokenBehavior create(Token token, TokenTemplateBehavior tokenTemplateBehavior) {
        TokenBehavior tokenBehavior = new TokenBehavior();
        tokenBehavior.setBehaviorId(tokenTemplateBehavior.getBehaviorId());
        tokenBehavior.setTokenId(token.getId());
        tokenBehavior = tokenBehaviorRepository.save(tokenBehavior);
        return tokenBehavior;
    }
    @Override
    public boolean isMintable(Token token) {
        return tokenBehaviorRepository.tokenHasBehavior(token.getId(), Behaviors.mintable.name());
    }
    @Override
    public boolean isDivisible(Token token) {
        return tokenBehaviorRepository.tokenHasBehavior(token.getId(), Behaviors.divisible.name());
    }
    @Override
    public boolean isTransferable(Token token) {
        return tokenBehaviorRepository.tokenHasBehavior(token.getId(), Behaviors.transferable.name());
    }
    @Override
    public boolean isBurnable(Token token) {
        return tokenBehaviorRepository.tokenHasBehavior(token.getId(), Behaviors.burnable.name());
    }
    @Override
    public List<Behavior> findTokenBehaviors(Long tokenId) {
        return tokenBehaviorRepository.findTokenBehaviors(tokenId);
    }
}    
