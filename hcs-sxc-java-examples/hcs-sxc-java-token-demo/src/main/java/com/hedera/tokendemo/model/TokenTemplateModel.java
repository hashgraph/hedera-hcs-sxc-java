package com.hedera.tokendemo.model;

import com.hedera.tokendemo.domain.TokenTemplate;

import java.util.Map;

public interface TokenTemplateModel {
    public boolean exists(String name);    
    public Map<String, String> list();
    public boolean tokenTemplatesExist();
    boolean isMintable(TokenTemplate Template);
    boolean isDivisible(TokenTemplate tokenTemplate);
    TokenTemplate findByName(String tokenTemplateName) throws Exception;
}