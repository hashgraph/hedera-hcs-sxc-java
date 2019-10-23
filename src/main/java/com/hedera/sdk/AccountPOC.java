package com.hedera.sdk;

import com.hedera.hashgraph.sdk.Client;
import com.hedera.hashgraph.sdk.HederaException;
import com.hedera.hashgraph.sdk.HederaNetworkException;
import com.hedera.hashgraph.sdk.account.AccountId;
import com.hedera.hashgraph.sdk.crypto.ed25519.Ed25519PrivateKey;
import com.hedera.hashgraph.sdk.crypto.ed25519.Ed25519PublicKey;
import com.hedera.hcslib.config.Config;

public class AccountPOC {

    public void create() throws HederaNetworkException, HederaException {
        
        Config config = new Config();
        // Generate a Ed25519 private, public key pair
        Ed25519PrivateKey newKey = Ed25519PrivateKey.generate();
        Ed25519PublicKey newPublicKey = newKey.getPublicKey();
        System.out.println("private key = " + newKey);
        System.out.println("public key = " + newPublicKey);

        Client client = new Client(config.getNodesMap());
        client.setOperator(
                AccountId.fromString(config.getOperatorAccountId())
                , config.getOperatorKey()
                );

        int maxTransactionFee = 100000000;
        AccountId newAccountId = client
                .setMaxTransactionFee(maxTransactionFee)
                .createAccount(newPublicKey, 100000000);

        System.out.println("account = " + newAccountId);
    }
}
