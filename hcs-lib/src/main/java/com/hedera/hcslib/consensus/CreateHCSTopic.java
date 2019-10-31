package com.hedera.hcslib.consensus;

import java.util.HashMap;
import java.util.Map;

import com.hedera.hashgraph.sdk.Client;
import com.hedera.hashgraph.sdk.HederaException;
import com.hedera.hashgraph.sdk.HederaNetworkException;
import com.hedera.hashgraph.sdk.TransactionReceipt;
import com.hedera.hashgraph.sdk.account.AccountId;
import com.hedera.hashgraph.sdk.consensus.CreateTopicTransaction;
import com.hedera.hashgraph.sdk.consensus.TopicId;
import com.hedera.hashgraph.sdk.crypto.ed25519.Ed25519PrivateKey;
import com.hedera.hcslib.HCSLib;

public final class CreateHCSTopic {
    private Map<AccountId, String> nodeMap = new HashMap<AccountId, String>();
    private AccountId operatorAccountId = new AccountId(0, 0, 0); 
    private Ed25519PrivateKey ed25519PrivateKey;
    
    public CreateHCSTopic(HCSLib hcsLib) {
        this.nodeMap = hcsLib.getNodeMap();
        this.operatorAccountId = hcsLib.getOperatorAccountId();
        this.ed25519PrivateKey = hcsLib.getEd25519PrivateKey();
    }

    public CreateHCSTopic overrideNodeMap(Map<AccountId, String> nodeMap) {
        this.nodeMap = nodeMap;
        return this;
    }
    public CreateHCSTopic overrideOperatorAccountId(AccountId operatorAccountId) {
        this.operatorAccountId = operatorAccountId;
        return this;
    }
    public CreateHCSTopic overrideOperatorKey(Ed25519PrivateKey ed25519PrivateKey) {
        this.ed25519PrivateKey = ed25519PrivateKey;
        return this;
    }

    /**
     * Creates a new topic on a Hedera network
     * @return TopicId
     * @throws HederaNetworkException
     * @throws IllegalArgumentException
     * @throws HederaException
     */
    public TopicId execute() throws HederaNetworkException, IllegalArgumentException, HederaException {

        Client client = new Client(this.nodeMap);
        client.setOperator(
            this.operatorAccountId
            ,this.ed25519PrivateKey
        );
        client.setMaxTransactionFee(100_000_000L);

        TransactionReceipt receipt = new CreateTopicTransaction(client)
                .executeForReceipt();
                
        return receipt.getTopicId();
    }

}
