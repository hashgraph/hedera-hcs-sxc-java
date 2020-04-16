package com.hedera.tokendemo.model;

import com.hedera.tokendemo.domain.UserAccount;

public interface UserAccountModel {

    boolean userAccountExists(long userId);

    UserAccount getForUserId(long userId) throws Exception;

    UserAccount create(long accountId, long userId);
}