package com.hedera.tokendemo.restcontrollers;

import com.hedera.tokendemo.domain.Token;
import com.hedera.tokendemo.integration.HCSMessages;
import com.hedera.tokendemo.restentities.TokenRestDetails;
import com.hedera.tokendemo.service.TokenService;
import com.hedera.tokendemo.service.UserService;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@Log4j2
@CrossOrigin(maxAge = 3600)
@RestController
public class TokenController {

    private final TokenService tokenService;
    private final UserService userService;
    private final HCSMessages hcsMessages;

    class Response {
        public String result;
        public Response(String result) {
            this.result = result;
        }
    }

    public TokenController(TokenService tokenService, UserService userService, HCSMessages hcsMessages) throws Exception {
        this.tokenService = tokenService;
        this.userService = userService;
        this.hcsMessages = hcsMessages;
    }

    @GetMapping(value = "/tokens", produces = "application/json")
    public List<TokenRestDetails> tokens() {
        List<TokenRestDetails> allTokens = new ArrayList<TokenRestDetails>();
        for (Token token : tokenService.list()) {
            String owner = userService.getNameFromId(token.getOwnerUserId());
            TokenRestDetails tokenListItem = new TokenRestDetails(token, tokenService, owner);
            allTokens.add(tokenListItem);
        }

        return allTokens;
    }

    @GetMapping(value = "/tokens/{tokenName}", produces = "application/json")
    public ResponseEntity<TokenRestDetails> tokenDetails(@PathVariable String tokenName) throws Exception {
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type", "application/json");

        try {
            Token token = tokenService.getByName(tokenName);
            String owner = userService.getNameFromId(token.getOwnerUserId());
            return new ResponseEntity<>(new TokenRestDetails(token, tokenService, owner), headers, HttpStatus.OK);
        } catch (Exception e) {
            log.error(e);
            throw e;
        }
    }


    @PostMapping(value = "/tokens", consumes = "application/json", produces = "application/json")
    public ResponseEntity<Response> tokenCreate(@RequestBody TokenRestDetails tokenRestCreate) {
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type", "application/json");
        try {
            Token token = new Token();
            token.setSymbol(tokenRestCreate.getSymbol());
            token.setCap(tokenRestCreate.getCap());
            token.setDecimals(tokenRestCreate.getDecimals());
            token.setName(tokenRestCreate.getName());
            hcsMessages.tokenCreate(token, tokenRestCreate.getTemplate(), tokenRestCreate.getOwner());
            return new ResponseEntity<>(new Response("Token create request in progress"), headers, HttpStatus.OK);
        } catch (Exception e) {
            log.error(e);
            throw e;
        }
    }

    @PostMapping(value = "/tokens/mint/{token}/{user}/{quantity}", produces = "application/json")
    public ResponseEntity<Response> tokenMint(@PathVariable String token, @PathVariable String user, @PathVariable long quantity) {
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type", "application/json");
        try {
            hcsMessages.tokenMint(token, quantity, user);
            return new ResponseEntity<>(new Response("Token mint request in progress"), headers, HttpStatus.OK);
        } catch (Exception e) {
            log.error(e);
            throw e;
        }
    }

    @PostMapping(value = "/tokens/burn/{token}/{user}/{quantity}", produces = "application/json")
    public ResponseEntity<Response> tokenBurn(@PathVariable String token, @PathVariable String user, @PathVariable long quantity) {
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type", "application/json");
        try {
            hcsMessages.tokenBurn(token, quantity, user);
            return new ResponseEntity<>(new Response("Token burn request in progress"), headers, HttpStatus.OK);
        } catch (Exception e) {
            log.error(e);
            throw e;
        }
    }

    @PostMapping(value = "/tokens/transfer/{token}/{user}/{amount}/{toAccount}", produces = "application/json")
    public ResponseEntity<Response> tokenTransfer(@PathVariable String token, @PathVariable String user, @PathVariable long amount, @PathVariable String toAccount) {
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type", "application/json");
        try {
            hcsMessages.tokenTransfer(token, toAccount, amount, user);
            return new ResponseEntity<>(new Response("Token transfer request in progress"), headers, HttpStatus.OK);
        } catch (Exception e) {
            log.error(e);
            throw e;
        }
    }

    @PostMapping(value = "/tokens/transferfrom/{token}/{user}/{amount}/{toAccount}", produces = "application/json")
    public ResponseEntity<Response> tokenTransferFrom(@PathVariable String token, @PathVariable String user, @PathVariable long amount, @PathVariable String toAccount) {
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type", "application/json");
        try {
            hcsMessages.tokenTransferFrom(token, toAccount, amount, user);
            return new ResponseEntity<>(new Response("Token transfer request in progress"), headers, HttpStatus.OK);
        } catch (Exception e) {
            log.error(e);
            throw e;
        }
    }
}
