package com.hedera.tokendemo.restentities;

import com.hedera.tokendemo.domain.Token;
import com.hedera.tokendemo.service.TokenService;
import lombok.Data;

@Data
public class TokenRestDetails {

    private long id;
    private String template;
    private String name;
    private String symbol;
    private Long balance;
    private Long cap;
    private long quantity;
    private String owner;
    private int decimals;
    // behaviors
    private boolean isPaused;
    private boolean isTransferable;
    private boolean isBurnable;
    private boolean isMintable;
    private boolean isDivisible;

    public TokenRestDetails() {

    }

    public TokenRestDetails(Token token, TokenService tokenService, String owner) {
        this.id = token.getId();
        this.name = token.getName();
        this.symbol = token.getSymbol();
        this.cap = token.getCap();
        this.balance = token.getBalance();
        this.quantity = token.getQuantity();
        this.owner = owner;
        this.decimals = token.getDecimals();

        this.isPaused = tokenService.isPaused(token);
        this.isTransferable = tokenService.isTransferable(token);
        this.isBurnable = tokenService.isBurnable(token);
        this.isMintable = tokenService.isMintable(token);
        this.isDivisible = tokenService.isDivisible(token);
    }
}
