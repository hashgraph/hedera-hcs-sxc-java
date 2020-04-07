package com.hedera.tokendemo.restcontrollers;

import com.hedera.tokendemo.service.AccountService;
import com.hedera.tokendemo.service.TokenService;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Log4j2
@CrossOrigin(maxAge = 3600)
@RestController
public class BalanceController {

    private final AccountService accountService;
    private final TokenService tokenService;

    public BalanceController(AccountService accountService, TokenService tokenService) throws Exception {
        this.accountService = accountService;
        this.tokenService = tokenService;
    }

    class BalanceItem {
        public String user;
        public long balance;
    }

    @GetMapping(value = "/balance/{tokenName}", produces = "application/json")
    public List<BalanceItem> balance(@PathVariable String tokenName) {
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type", "application/json");

        try {
            long tokenId = tokenService.getIdForName(tokenName);
            Map<String, Long> balances = accountService.allBalancesForToken(tokenId);
            List<BalanceItem> allBalances = new ArrayList<BalanceItem>();
            for (Map.Entry<String, Long> balance : balances.entrySet()) {
                BalanceItem balanceItem = new BalanceItem();
                balanceItem.balance = balance.getValue();
                balanceItem.user = balance.getKey();
                allBalances.add(balanceItem);
            }

            return allBalances;
        } catch (Exception e) {
            log.error(e);
            throw e;
        }
    }

    @GetMapping(value = "/balance/{tokenName}/{userName}", produces = "application/json")
    public long balance(@PathVariable String tokenName, @PathVariable String userName) throws Exception {
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type", "application/json");

        try {
            long tokenId = tokenService.getIdForName(tokenName);
            return accountService.balanceForUser(tokenId, userName);
        } catch (Exception e) {
            log.error(e);
            throw e;
        }
    }
}
