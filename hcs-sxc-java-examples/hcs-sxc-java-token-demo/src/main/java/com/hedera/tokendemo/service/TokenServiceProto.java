package com.hedera.tokendemo.service;

import com.google.protobuf.Any;
import com.hedera.tokendemo.domain.Token;
import com.hedera.tokendemo.domain.TokenTemplate;
import com.hedera.tokendemo.model.TokenModelProto;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import org.tti.ttf.taxonomy.model.artifact.*;
import org.tti.ttf.taxonomy.model.core.Base;

@Service
@Log4j2
public class TokenServiceProto implements TokenModelProto {

    private final TokenService tokenService;
    private final TokenTemplateService tokenTemplateService;

    public TokenServiceProto(TokenService tokenService, TokenTemplateService tokenTemplateService) {
        this.tokenService = tokenService;
        this.tokenTemplateService = tokenTemplateService;
    }

    @Override
    public void create(NewArtifactRequest newArtifactRequest) throws Exception {
        
        Token token = new Token();
        Any anyArtifactBase = newArtifactRequest.getArtifact();

        Base base = anyArtifactBase.unpack(Base.class);
        token.setCap(base.getQuantity());
        token.setDecimals(base.getDecimals());
        token.setName(base.getName());
        token.setSymbol(base.getSymbol());

        TokenTemplate tokenTemplate = tokenTemplateService.findByName(base.getConstructorName());
        String ownerName = base.getOwner();
        tokenService.create(ownerName, token, tokenTemplate.getId());
    }
    
    @Override 
    public void mint(MintRequest mintRequest) throws Exception {

        long quantity = Long.parseLong(mintRequest.getQuantity().toStringUtf8());
        MessageHeader messageHeader = mintRequest.getHeader();
        Any anyHcsTokenOperation = messageHeader.getCustomMessageHeader();
        HCSTokenOperation hcsTokenOperation = anyHcsTokenOperation.unpack(HCSTokenOperation.class);

        String userName = hcsTokenOperation.getOperatorName();
        String tokenName = hcsTokenOperation.getTokenName();
        
        tokenService.mint(userName, tokenName, quantity);
    }

    @Override 
    public void transfer(TransferRequest transferRequest) throws Exception {

        long amount = Long.parseLong(transferRequest.getQuantity().toStringUtf8());
        String toAccount = transferRequest.getToAccountId();
        MessageHeader messageHeader = transferRequest.getHeader();
        Any anyHcsTokenOperation = messageHeader.getCustomMessageHeader();
        HCSTokenOperation hcsTokenOperation = anyHcsTokenOperation.unpack(HCSTokenOperation.class);

        String userName = hcsTokenOperation.getOperatorName();
        String tokenName = hcsTokenOperation.getTokenName();
        
        tokenService.transfer(userName, tokenName, toAccount, amount);
    }
    @Override 
    public void transferFrom(TransferFromRequest transferFromRequest) throws Exception {

        long amount = Long.parseLong(transferFromRequest.getQuantity().toStringUtf8());
        String toAccountName = transferFromRequest.getToAccountId();
        String fromAccountName = transferFromRequest.getFromAccountId();
        MessageHeader messageHeader = transferFromRequest.getHeader();
        Any anyHcsTokenOperation = messageHeader.getCustomMessageHeader();
        HCSTokenOperation hcsTokenOperation = anyHcsTokenOperation.unpack(HCSTokenOperation.class);

//        String userName = hcsTokenOperation.getOperatorName();
        String tokenName = hcsTokenOperation.getTokenName();
        
        tokenService.transferFrom(fromAccountName, tokenName, toAccountName, amount);
    }
    @Override 
    public void burn(BurnRequest burnFromRequest) throws Exception {

        long burnQuantity = Long.parseLong(burnFromRequest.getQuantity().toStringUtf8());
        MessageHeader messageHeader = burnFromRequest.getHeader();
        Any anyHcsTokenOperation = messageHeader.getCustomMessageHeader();
        HCSTokenOperation hcsTokenOperation = anyHcsTokenOperation.unpack(HCSTokenOperation.class);

        String userName = hcsTokenOperation.getOperatorName();
        String tokenName = hcsTokenOperation.getTokenName();
        
        tokenService.burn(userName, tokenName, burnQuantity);
    }
}