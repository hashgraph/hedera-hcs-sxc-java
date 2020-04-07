package com.hedera.tokendemo.service;

import com.hedera.tokendemo.domain.UserAccount;
import com.hedera.tokendemo.model.UserAccountModel;
import com.hedera.tokendemo.repository.UserAccountRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserAccountService implements UserAccountModel {

    private final UserAccountRepository userAccountRepository;

    public UserAccountService(UserAccountRepository userAccountRepository) {
        this.userAccountRepository = userAccountRepository;
    }

    @Override
    public boolean userAccountExists(long userId) {
        return userAccountRepository.findByUserId(userId).isPresent();
    }
    
    @Override
    public UserAccount getForUserId(long userId) throws RuntimeException {
        Optional<UserAccount> userAccount = userAccountRepository.findByUserId(userId);
        if (userAccount.isPresent()) {
            return userAccount.get();
        } else {
            throw new RuntimeException("User account for id " + userId + " not found");
        }
    }
    
    @Override
    public UserAccount create(long accountId, long userId) {
        UserAccount userAccount = new UserAccount();
        userAccount.setAccountId(accountId);
        userAccount.setUserId(userId);
        return userAccountRepository.save(userAccount);
    }

}
