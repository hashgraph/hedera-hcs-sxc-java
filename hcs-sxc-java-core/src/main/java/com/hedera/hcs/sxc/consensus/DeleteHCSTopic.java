package com.hedera.hcs.sxc.consensus;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

import com.hedera.hashgraph.sdk.Client;
import com.hedera.hashgraph.sdk.HederaNetworkException;
import com.hedera.hashgraph.sdk.HederaStatusException;
import com.hedera.hashgraph.sdk.TransactionId;
import com.hedera.hashgraph.sdk.TransactionReceipt;
import com.hedera.hashgraph.sdk.account.AccountId;
import com.hedera.hashgraph.sdk.consensus.ConsensusTopicDeleteTransaction;
import com.hedera.hashgraph.sdk.consensus.ConsensusTopicId;
import com.hedera.hashgraph.sdk.crypto.ed25519.Ed25519PrivateKey;
import com.hedera.hcs.sxc.HCSCore;
    
public final class DeleteHCSTopic {
    private Map<AccountId, String> nodeMap = new HashMap<AccountId, String>();
    private AccountId operatorAccountId = new AccountId(0, 0, 0); 
    private Ed25519PrivateKey ed25519PrivateKey;
    private ConsensusTopicDeleteTransaction tx = new ConsensusTopicDeleteTransaction();
    
    public DeleteHCSTopic(HCSCore hcsCore) {
        this.nodeMap = hcsCore.getNodeMap();
        this.operatorAccountId = hcsCore.getOperatorAccountId();
        this.ed25519PrivateKey = hcsCore.getEd25519PrivateKey();
        this.tx.setMaxTransactionFee(hcsCore.getMaxTransactionFee());
    }
    
    public DeleteHCSTopic overrideNodeMap(Map<AccountId, String> nodeMap) {
        this.nodeMap = nodeMap;
        return this;
    }
    public DeleteHCSTopic overrideOperatorAccountId(AccountId operatorAccountId) {
        this.operatorAccountId = operatorAccountId;
        return this;
    }
    public DeleteHCSTopic overrideOperatorKey(Ed25519PrivateKey ed25519PrivateKey) {
        this.ed25519PrivateKey = ed25519PrivateKey;
        return this;
    }
    public DeleteHCSTopic setMaxTransactionFee(Long maxTansactionFee) {
        this.tx.setMaxTransactionFee(maxTansactionFee);
        return this;
    }
    public DeleteHCSTopic setTransactionMemo(String transactionMemo) {
        this.tx.setTransactionMemo(transactionMemo);
        return this;
    }
    public DeleteHCSTopic setTopicId(ConsensusTopicId topicId) {
        this.tx.setTopicId(topicId);
        return this;
    }
    public DeleteHCSTopic setTopicId(String topicId) {
        this.tx.setTopicId(ConsensusTopicId.fromString(topicId));
        return this;
    }
    
    /**
     * Deletes a topic on a Hedera network
     * @return ConsensusTopicId
     * @throws HederaNetworkException
     * @throws IllegalArgumentException
     * @throws HederaException
     * @throws IOException 
     * @throws FileNotFoundException 
     * @throws HederaStatusException 
     */
    public ConsensusTopicId execute() throws HederaNetworkException, IllegalArgumentException, FileNotFoundException, IOException, HederaStatusException {
    
        Client client = new Client(this.nodeMap);
        client.setOperator(
            this.operatorAccountId
            ,this.ed25519PrivateKey
        );
        
        TransactionId txId = tx.execute(client);
        TransactionReceipt receipt = txId.getReceipt(client, Duration.ofSeconds(30));
        
        return receipt.getConsensusTopicId();
    }
}

