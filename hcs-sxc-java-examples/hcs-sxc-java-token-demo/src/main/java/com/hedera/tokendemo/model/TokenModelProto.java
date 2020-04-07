package com.hedera.tokendemo.model;

import org.tti.ttf.taxonomy.model.artifact.*;

public interface TokenModelProto {

    void create(NewArtifactRequest newArtifactRequest) throws Exception;

    void mint(MintRequest mintRequest) throws Exception;

    void transfer(TransferRequest transferRequest) throws Exception;

    void transferFrom(TransferFromRequest transferFromRequest) throws Exception;

    void burn(BurnRequest burnFromRequest) throws Exception;
}