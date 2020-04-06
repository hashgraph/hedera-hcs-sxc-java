package com.hedera.tokendemo.service;

import com.hedera.tokendemo.domain.Account;
import com.hedera.tokendemo.domain.Token;
import com.hedera.tokendemo.domain.User;
import com.hedera.tokendemo.model.AccountModel;
import com.hedera.tokendemo.repository.AccountRepository;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class AccountService implements AccountModel {
    private final AccountRepository accountRepository;
    private final UserService userService;
    private final UserAccountService userAccountService;

    public AccountService(AccountRepository accountRepository, UserService userService, UserAccountService userAccountService) {
        this.accountRepository = accountRepository;
        this.userService = userService;
        this.userAccountService = userAccountService;
    }

    @Override
    public boolean exists(Token token, long userId) {
        return accountRepository.findByTokenAndUserId(token.getId(), userId).size() > 0;
    }

    @Override
    public Account findByTokenAndUserId(Token token, long userId) throws RuntimeException {
        List<Account> account = accountRepository.findByTokenAndUserId(token.getId(), userId);
        if (account.size() > 0) {
            return account.get(0);
        } else {
            throw new RuntimeException ("Account not found for token " + token.getName() + " and user id " + userId);
        }
    }

    @Override
    public Account findByTokenAndUserName(Token token, String userName) throws RuntimeException {
        long userId = userService.getIdFromName(userName);
        return findByTokenAndUserId(token, userId);
    }

    @Override
    public Account create(Token token, long balance) {
        Account account = new Account();
        account.setBalance(0);
        account.setTokenId(token.getId());
        account = accountRepository.save(account);
        return account;
   }
    
   @Override
   public Account update(Account account) {
       return accountRepository.save(account);
   }
   
   public Map<String, Long> allBalancesForToken(long tokenId) throws RuntimeException {
       Map<String, Long> balances = new HashMap<String, Long>();
       
       List<Account> accounts = accountRepository.findByTokenId(tokenId);
       for (Account account : accounts) {
           List<User> users = userService.usersForAccount(account.getId());
           String usersList = "";
           for (User user : users) {
               if (usersList.isEmpty()) {
                   usersList = user.getName();
               } else {
                   usersList = usersList.concat(", ").concat(user.getName());
               }
           }
           balances.put(usersList, account.getBalance());
       }
       
       return balances;
   }
   
   @Override
   public long balanceForUser(long tokenId, String userName) throws RuntimeException {

       long userId = userService.getIdFromName(userName);
       List<Account> account = accountRepository.findByTokenAndUserId(tokenId, userId);

       if (account.size() > 0) {
           long balance = account.get(0).getBalance();
           return balance;
       } else {
           return 0;
       }
   }

    @Override
    public Account createIfNotFound(String userName, String userPubKey, String role, Token token) {
        long userId;
        try {
            userId = userService.getIdFromName(userName);
        } catch (RuntimeException e) {
            // user doesn't exist, create it
            userService.create(userName,userPubKey, role);
            userId = userService.getIdFromName(userName);

        }
        Account account;
        try {
            account = findByTokenAndUserId(token, userId);
        } catch (RuntimeException e) {
            // need to create an account and link it to the user
            account = create(token, 0);
            userAccountService.create(account.getId(), userId);
        }

        return account;
    }
}
