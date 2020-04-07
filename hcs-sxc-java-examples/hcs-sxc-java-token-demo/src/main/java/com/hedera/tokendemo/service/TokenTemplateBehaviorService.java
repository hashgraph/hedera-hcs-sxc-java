package com.hedera.tokendemo.service;

import com.hedera.tokendemo.domain.Token;
import com.hedera.tokendemo.domain.TokenTemplate;
import com.hedera.tokendemo.domain.TokenTemplateBehavior;
import com.hedera.tokendemo.model.TokenTemplateBehaviorModel;
import com.hedera.tokendemo.repository.TokenTemplateBehaviorRepository;
import com.hedera.tokendemo.utils.Behaviors;
import org.springframework.stereotype.Service;

@Service
public class TokenTemplateBehaviorService implements TokenTemplateBehaviorModel {

    private final TokenTemplateBehaviorRepository tokenTemplateBehaviorRepository;
    private  final TokenBehaviorService tokenBehaviorService;

    public TokenTemplateBehaviorService(TokenTemplateBehaviorRepository tokenTemplateBehaviorRepository
        ,TokenBehaviorService tokenBehaviorService) {
        this.tokenTemplateBehaviorRepository = tokenTemplateBehaviorRepository;
        this.tokenBehaviorService = tokenBehaviorService;
    }

    @Override
    public boolean isMintable(TokenTemplate tokenTemplate) {
        return tokenTemplateBehaviorRepository.tokenTemplateHasBehavior(tokenTemplate.getId(), Behaviors.mintable.name());
    }
    @Override
    public boolean isDivisible(TokenTemplate tokenTemplate) {
        return tokenTemplateBehaviorRepository.tokenTemplateHasBehavior(tokenTemplate.getId(), Behaviors.divisible.name());
    }
    @Override
    public void copyBehaviorsToToken(long tokenTemplateId, Token token) {
        // copy behaviors from template
        for (TokenTemplateBehavior tokenTemplateBehavior : tokenTemplateBehaviorRepository.getBehaviors(tokenTemplateId)) {
            tokenBehaviorService.create(token, tokenTemplateBehavior);
        }
    }
    
}
