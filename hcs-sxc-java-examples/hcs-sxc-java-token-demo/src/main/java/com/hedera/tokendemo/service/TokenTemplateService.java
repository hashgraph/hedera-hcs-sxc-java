package com.hedera.tokendemo.service;

import com.hedera.tokendemo.domain.TokenTemplate;
import com.hedera.tokendemo.model.TokenTemplateModel;
import com.hedera.tokendemo.repository.TokenTemplateRepository;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
public class TokenTemplateService implements TokenTemplateModel {

    private final TokenTemplateRepository tokenTemplateRepository;
    private final TokenTemplateBehaviorService tokenTemplateBehaviorService;

    public TokenTemplateService(TokenTemplateRepository tokenTemplateRepository, TokenTemplateBehaviorService tokenTemplateBehaviorService) {
        this.tokenTemplateRepository = tokenTemplateRepository;
        this.tokenTemplateBehaviorService = tokenTemplateBehaviorService;
    }

    @Override
    public boolean exists(String name) {
        return tokenTemplateRepository.findByName(name).isPresent();
    }
    @Override
    public TokenTemplate findByName(String tokenTemplateName) throws RuntimeException {
        Optional<TokenTemplate> tokenTemplate = tokenTemplateRepository.findByName(tokenTemplateName);
        if (tokenTemplate.isPresent()) {
            return tokenTemplate.get();
        } else {
            throw new RuntimeException ("Token template " + tokenTemplateName + " not found.");
        }
    }
    @Override
    public boolean isMintable(TokenTemplate tokenTemplate) {
        return tokenTemplateBehaviorService.isMintable(tokenTemplate);
    }
    @Override
    public boolean isDivisible(TokenTemplate tokenTemplate) {
        return tokenTemplateBehaviorService.isDivisible(tokenTemplate);
    }

    @Override
    public Map<String, String> list() {
        Map<String, String> list = new HashMap<String, String>();
        int index = 1;
        for (TokenTemplate tokenType : tokenTemplateRepository.findAll()) {
            list.put(Integer.toString(index), tokenType.getName());
            index++;
        }
        return list;
    }
    @Override
    public boolean tokenTemplatesExist() {
        return (tokenTemplateRepository.count() > 0);
    }
}