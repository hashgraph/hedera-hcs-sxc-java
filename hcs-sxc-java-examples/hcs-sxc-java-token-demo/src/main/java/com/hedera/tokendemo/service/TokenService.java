package com.hedera.tokendemo.service;

import com.hedera.tokendemo.config.AppConfig;
import com.hedera.tokendemo.domain.Account;
import com.hedera.tokendemo.domain.Behavior;
import com.hedera.tokendemo.domain.Token;
import com.hedera.tokendemo.model.TokenModel;
import com.hedera.tokendemo.notifications.CustomStompSessionHandler;
import com.hedera.tokendemo.notifications.NotificationMessage;
import com.hedera.tokendemo.repository.TokenRepository;
import lombok.extern.log4j.Log4j2;
import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.messaging.simp.stomp.StompSession;
import org.springframework.messaging.simp.stomp.StompSessionHandler;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.client.WebSocketClient;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ExecutionException;

@Log4j2
@Service
public class TokenService implements TokenModel {

    private final TokenRepository tokenRepository;
    private final AccountService accountService;
    private final UserAccountService userAccountService;
    private final UserService userService;
    private final TokenBehaviorService tokenBehaviorService;
    private final TokenTemplateBehaviorService tokenTemplateBehaviorService;
    private final AppConfig appConfig;
    private final OperationService operationService;

    private StompSession stompSession;

    public TokenService(TokenRepository tokenRepository, AccountService accountService, UserAccountService userAccountService, UserService userService, TokenBehaviorService tokenBehaviorService, TokenTemplateBehaviorService tokenTemplateBehaviorService, AppConfig appConfig, OperationService operationService) {
        this.tokenRepository = tokenRepository;
        this.accountService = accountService;
        this.userAccountService = userAccountService;
        this.userService = userService;
        this.tokenBehaviorService = tokenBehaviorService;
        this.tokenTemplateBehaviorService = tokenTemplateBehaviorService;
        this.appConfig = appConfig;
        this.operationService = operationService;
    }

    @Override
    public boolean exists(String tokenName) {
        Optional<Token> token = tokenRepository.findByName(tokenName);
        if (token.isPresent()) {
            return true;
        } else {
            return false;
        }
    }

    @Override
    public boolean symbolExists(String tokenSymbol) {
        Optional<Token> token = tokenRepository.findBySymbol(tokenSymbol);
        if (token.isPresent()) {
            return true;
        } else {
            return false;
        }
    }

    @Override
    public long getIdForName(String tokenName) throws RuntimeException {
        Optional<Token> token = tokenRepository.findByName(tokenName);
        if (token.isPresent()) {
            return token.get().getId();
        } else {
            notifyWithException("Token " + tokenName + " not found");
            return 0L;
        }
    }

    @Override
    public Token getByName(String tokenName) throws RuntimeException {
        Optional<Token> token = tokenRepository.findByName(tokenName);
        if (token.isPresent()) {
            return token.get();
        } else {
            notifyWithException("Token " + tokenName + " not found");
            return new Token();
        }
    }

    @Override
    public Token create(String userName, Token token, long tokenTemplateId) throws RuntimeException {
        if (exists(token.getName())) {
            notifyWithException("Token with name=" + token.getName() + " already exists");
        }
        if (symbolExists(token.getSymbol())) {
            notifyWithException("Token with symbol=" + token.getSymbol() + " already exists");
        }
        //TODO: public key
        userService.create(userName, "", "Consumer");

        token.setSymbol(token.getSymbol().toUpperCase());
        token.setOwnerUserId(userService.getIdFromName(userName));
        token = tokenRepository.save(token);

        tokenTemplateBehaviorService.copyBehaviorsToToken(tokenTemplateId, token);
        operationService.create(userName, "", "Created token " + token.getName());

        notify(true, "Token creation successful for : " + token.getName());
        return token;
    }

    @Override
    public Token update(Token token) {
        return tokenRepository.save(token);
    }

    @Override
    public boolean isMintable(Token token) {
        return tokenBehaviorService.isMintable(token);
    }

    @Override
    public boolean isTransferable(Token token) {
        return tokenBehaviorService.isTransferable(token);
    }

    @Override
    public boolean isDivisible(Token token) {
        return tokenBehaviorService.isDivisible(token);
    }
    @Override
    public boolean isBurnable(Token token) {
        return tokenBehaviorService.isBurnable(token);
    }

    @Override
    public boolean isPaused(Token token) {
        //TODO: Implement
        return false;
    }

    @Override
    public String toString(Token token) throws RuntimeException {
        String tokenDetails = "";

        String userName = userService.getNameFromId(token.getOwnerUserId());

        tokenDetails = tokenDetails.concat("Name: ").concat(token.getName());
        tokenDetails = tokenDetails.concat("\n  - Symbol: ").concat(token.getSymbol());
        tokenDetails = tokenDetails.concat("\n  - Cap: ").concat(Long.toString(token.getCap()));
        tokenDetails = tokenDetails.concat("\n  - Decimals: ").concat(Integer.toString(token.getDecimals()));
        tokenDetails = tokenDetails.concat("\n  - Quantity: ").concat(Long.toString(token.getQuantity()));
        tokenDetails = tokenDetails.concat("\n  - Balance: ").concat(Long.toString(token.getBalance()));
        tokenDetails = tokenDetails.concat("\n  - Owner: ").concat(userName);

        String behaviors = "";
        for (Behavior behavior : tokenBehaviorService.findTokenBehaviors(token.getId())) {
            behaviors = behaviors.concat("\n    - ").concat(behavior.getName());
        }
        if (!behaviors.isEmpty()) {
            tokenDetails = tokenDetails.concat("\n  Behaviors:");
            tokenDetails = tokenDetails.concat(behaviors);
        }

        return tokenDetails;

    }

//    @Override
//    public Pair<Boolean, String> pause(String tokenName) {
//        Optional<Token> token = tokenRepository.findByName(tokenName);
//        if (token.isPresent()) { 
//            token.get().setPaused(true);
//            tokenRepository.save(token.get());
//            return Pair.of(true, "Token paused");
//        } else {
//            return Pair.of(false, "Token not found");
//        }
//    }

//    @Override
//    public Pair<Boolean, String> unPause(String tokenName) {
//        Optional<Token> token = tokenRepository.findByName(tokenName);
//        if (token.isPresent()) { 
//            token.get().setPaused(false);
//            tokenRepository.save(token.get());
//            return Pair.of(true, "Token un-paused");
//        } else {
//            return Pair.of(false, "Token not found");
//        }
//    }

    @Override
    public void burn(String userName, String tokenName, long burnQuantity) throws RuntimeException {
        Optional<Token> tokenExists = tokenRepository.findByName(tokenName);

        if (!tokenExists.isPresent()) {
            notifyWithException("Token " + tokenName + " doesn't exists");
        } else {
            Token token = tokenExists.get();
            if (!isBurnable(token)) {
                notifyWithException("Token " + tokenName + " is not burnable");
            }
            if (isPaused(token)) {
                notifyWithException("No minting allowed while token is paused.");
            }
            // TODO: Check allowed burners ?
            if (token.getOwnerUserId() != userService.getIdFromName(userName)) {
                notifyWithException("(" + tokenName + ") Only the token's owner or approved minters may mint it");
            }
            if (burnQuantity > token.getBalance()) {
                notifyWithException("(" + tokenName + ") Cannot burn more than current token balance");
            }
            
            if (burnQuantity == 0) {
                // reduce quantity by current balance and reduce balance to 0
                token.setQuantity(token.getQuantity() - token.getBalance());
                token.setBalance(0L);
            } else {
                // reduce balance and quantity by burn amount
                token.setBalance(token.getBalance() - burnQuantity);
                token.setQuantity(token.getQuantity() - burnQuantity);
            }

            token = tokenRepository.save(token);
            operationService.create(userName, "", "Burnt " + token.getName() + "(" + burnQuantity + ")");
            notify(true, "Token burn successful for : " + token.getName());

        }
    }

    @Override
    public List<Token> list() {
        return tokenRepository.findAllByOrderByIdDesc();
    }

    @Override
    public void transfer(String userName, String tokenName, String toAccountName, long amount) throws RuntimeException {
        Token token = tokenExists(tokenName);
        if (!isTransferable(token)) {
            notifyWithException("Token (" + tokenName + ") doesn't support transfers");
        }
        if (isPaused(token)) {
            notifyWithException("No transfers allowed while token is paused.");
        }
        // check user is owner
        try {
            checkUserIsOwner(userName, token);
        } catch (RuntimeException e) {
            notifyWithException("(" + tokenName + ") Only the token's owner or approved minters may transfer from it");
        }

        if (token.getBalance() >= amount) {
            // destination user id
            //TODO: PUBKey
            Account toAccount = accountService.createIfNotFound(toAccountName, "", "Consumer", token);
            // set balances
            //TODO: Should really be inside a transaction
            token.setBalance(token.getBalance() - amount);
            tokenRepository.save(token);
            toAccount.setBalance(toAccount.getBalance() + amount);
            accountService.update(toAccount);
            notify(true, "Token transfer successful for : " + token.getName());
            operationService.create(userName, toAccountName, "Transfer " + token.getName() + "(" + amount + ")");
        } else {
            notifyWithException("(" + tokenName + ") Insufficient token balance (" + token.getBalance() + ")");
        }
    }

    @Override
    public void transferFrom(String fromAccountName, String tokenName, String toAccountName, long amount) throws RuntimeException {
        Token token = tokenExists(tokenName);
        if (!isTransferable(token)) {
            notifyWithException("Token (" + tokenName + ") doesn't support transfers");
        }
        if (isPaused(token)) {
            notifyWithException("No transfers allowed while token is paused.");
        }
        // check user balance is sufficient
        //TODO: PUBKey
        Account fromAccount = accountService.findByTokenAndUserName(token, fromAccountName);
        if (fromAccount.getBalance() < amount) {
            notifyWithException("(" + tokenName + ") TransferFrom, account balance (" + fromAccount.getBalance() + ") is lower than requested amount (" + amount +")");
        }

        //TODO: PUBKey
        Account toAccount = accountService.createIfNotFound(toAccountName, "", "Consumer", token);

        // set balances
        fromAccount.setBalance((fromAccount.getBalance() - amount));
        toAccount.setBalance((toAccount.getBalance() + amount));
        //TODO: Should really be inside a transaction
        accountService.update(fromAccount);
        accountService.update(toAccount);
        operationService.create(fromAccountName, toAccountName, "Transfer From " + token.getName() + "(" + amount + ")");
        notify(true, "Token transfer successful for : " + token.getName());
    }

    @Override
    public Token mint(String userName, String name, long mintQuantity) throws RuntimeException {
        Optional<Token> tokenExists = tokenRepository.findByName(name);

        if (!tokenExists.isPresent()) {
            notifyWithException("Token " + name + " doesn't exists");
            return new Token();
        } else {
            Token token = tokenExists.get();

            if (!isMintable(token)) {
                notifyWithException("Token " + token.getName() + " is not mintable");
            }
            if (isPaused(token)) {
                notifyWithException("No minting allowed while token is paused.");
            }
            // TODO: Check allowed minters
            if (token.getOwnerUserId() != userService.getIdFromName(userName)) {
                notifyWithException("(" + name + ") Only the token's owner or approved minters may mint it");
            }

            // if 0, set to cap
            // TODO: Check if cap is unlimited, or gated
            if (mintQuantity == 0) {
                // increase balance with difference between old and new quantities
                token.setBalance(token.getBalance() + (token.getCap() - token.getQuantity()));
                token.setQuantity(token.getCap());
            } else {
                // increase balance with new quantity
                token.setBalance(token.getBalance() + mintQuantity);
                token.setQuantity(token.getQuantity() + mintQuantity);
            }
            if (token.getQuantity() > token.getCap()) {
                notifyWithException("(" + name + ") Mint, new quantity exceeds cap (" + token.getCap() + ")");
            }

            token = tokenRepository.save(token);
            operationService.create(userName, "", "Minted " + token.getName() + "(" + mintQuantity + ")");
            notify(true, "Token minting successful for : " + token.getName());
            return token;
        }
    }

    @Override
    public Map<String, String> listAsMap() {
        List<Token> tokens = list();
        Map<String, String> tokenList = new HashMap<>();
        int i = 1;
        for (Token token : tokens) {
            tokenList.put(String.valueOf(i), token.getName());
            i++;
        }
        return tokenList;
    }

    private Token tokenExists(String tokenName) throws RuntimeException {
        Optional<Token> tokenExists = tokenRepository.findByName(tokenName);
        if (tokenExists.isPresent()) {
            return tokenExists.get();
        } else {
            notifyWithException("Token " + tokenName + " unknown");
            return new Token();
        }
    }

    private void checkUserIsOwner(String userName, Token token) throws RuntimeException {
        if (token.getOwnerUserId() != userService.getIdFromName(userName)) {
            notifyWithException("(" + token.getName() + ") Current user " + userName + " isn't the token's owner");
        }
    }
    private void notifyWithException(String message) throws RuntimeException {
        notify(false, message);
        throw new RuntimeException(message);
    }
    private void notify(boolean success, String message) {
        if ((this.stompSession == null) || ( ! this.stompSession.isConnected())) {
            WebSocketClient client = new StandardWebSocketClient();
            WebSocketStompClient stompClient = new WebSocketStompClient(client);
            stompClient.setMessageConverter(new MappingJackson2MessageConverter());
            StompSessionHandler sessionHandler = new CustomStompSessionHandler();
            try {
                int port = this.appConfig.port;
                this.stompSession = stompClient.connect("ws://localhost:" + port + "/notifications", sessionHandler).get();
            } catch (InterruptedException | ExecutionException e) {
                log.error(e);
            }
        }

        if (this.stompSession != null) {
            NotificationMessage notificationMessage = new NotificationMessage();
            String prefix = success ? "OK" : "ERROR";
            notificationMessage.setMessage(prefix + message);
            notificationMessage.setStatus(prefix);
            this.stompSession.send("/hcsapp/notifications",notificationMessage);
        }
    }
}