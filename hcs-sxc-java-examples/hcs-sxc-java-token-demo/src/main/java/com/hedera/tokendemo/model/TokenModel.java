package com.hedera.tokendemo.model;

import com.hedera.tokendemo.domain.Token;

import java.util.List;
import java.util.Map;

public interface TokenModel {

    Token create(String userName, Token token, long tokenTemplateId) throws RuntimeException;
    Token update(Token token);
    Token mint(String userName, String name, long mintQuantity) throws RuntimeException;
    void burn(String userName, String tokenName, long burnQuantity) throws RuntimeException;

    void transfer(String userName, String tokenName, String accountName, long amount) throws RuntimeException;
    void transferFrom(String userName, String tokenName, String accountName, long amount) throws RuntimeException;

    long getIdForName(String tokenName) throws RuntimeException;
    Token getByName(String tokenName) throws RuntimeException;
    boolean symbolExists(String tokenSymbol);

//    Pair<Boolean, String> pause(String name);
//    Pair<Boolean, String> unPause(String name);
    
    List<Token> list();
    Map<String, String> listAsMap();

    // behaviors
    boolean isBurnable(Token token);
    boolean isMintable(Token token);
    boolean isDivisible(Token token);
    boolean isTransferable(Token token);

    // properties
    boolean isPaused(Token token);
    String toString(Token token) throws RuntimeException;
    boolean exists(String name);
}