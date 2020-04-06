package com.hedera.tokendemo.model;

import com.hedera.tokendemo.domain.Account;
import com.hedera.tokendemo.domain.Token;

public interface AccountModel {

    Account findByTokenAndUserId(Token token, long userId) throws RuntimeException;

    Account findByTokenAndUserName(Token token, String userName) throws RuntimeException;

    Account create(Token token, long balance);

    Account createIfNotFound(String userName, String userPubKey, String role, Token token);

    boolean exists(Token token, long userId);

    Account update(Account account);

    long balanceForUser(long tokenId, String userName) throws RuntimeException;
}