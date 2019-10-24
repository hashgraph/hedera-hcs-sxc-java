package com.hedera.sdk;

import java.io.FileNotFoundException;
import java.io.IOException;

import com.hedera.hashgraph.sdk.Client;
import com.hedera.hashgraph.sdk.HederaException;
import com.hedera.hashgraph.sdk.HederaNetworkException;
import com.hedera.hashgraph.sdk.TransactionId;
import com.hedera.hashgraph.sdk.account.AccountId;
import com.hedera.hashgraph.sdk.account.CryptoTransferTransaction;
import com.hedera.hcslib.config.ConfigLoader;
import com.hedera.hcslib.config.Environment;

public class HCSMessage {
    public void send(String message) throws HederaNetworkException, HederaException, FileNotFoundException, IOException {
        Environment environment = new Environment();
        ConfigLoader configLoader = new ConfigLoader();

        Client client = new Client(configLoader.getNodesMap());
        client.setOperator(
            environment.getOperatorAccountId()
            ,environment.getOperatorKey()
        );

        TransactionId id = new TransactionId(environment.getOperatorAccountId()); 
        client.setMaxTransactionFee(100_000_000L);
        
        id = new CryptoTransferTransaction(client)
            .addSender(environment.getOperatorAccountId(), 1)
            .addRecipient(AccountId.fromString("0.0.3"), 1)
            .setMemo(message)
            .setTransactionId(id)
            .execute();
        
    }
}
