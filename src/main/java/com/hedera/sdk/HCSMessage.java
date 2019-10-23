package com.hedera.sdk;

import com.hedera.hashgraph.sdk.Client;
import com.hedera.hashgraph.sdk.HederaException;
import com.hedera.hashgraph.sdk.HederaNetworkException;
import com.hedera.hashgraph.sdk.TransactionId;
import com.hedera.hashgraph.sdk.account.AccountId;
import com.hedera.hashgraph.sdk.account.CryptoTransferTransaction;
import com.hedera.hcslib.config.Config;

public class HCSMessage {
    public void send(String message) throws HederaNetworkException, HederaException {
        Config config = new Config();

        Client client = new Client(config.getNodesMap());
        client.setOperator(
                config.getOperatorAccountId()
                , config.getOperatorKey()
                );

        TransactionId id = new TransactionId(config.getOperatorAccountId()); 
        client.setMaxTransactionFee(100_000_000L);
        
        CryptoTransferTransaction tx = new CryptoTransferTransaction(client)
            .addSender(config.getOperatorAccountId(), 1)
            .addRecipient(AccountId.fromString("0.0.2"), 1)
            .setMemo(message)
            .setTransactionId(id);
        
        tx.execute();
    }
}
