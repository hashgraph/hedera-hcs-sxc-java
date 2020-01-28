package com.hedera.hcs.sxc.consensus;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

import com.hedera.hashgraph.sdk.Client;
import com.hedera.hashgraph.sdk.HederaNetworkException;
import com.hedera.hashgraph.sdk.HederaStatusException;
import com.hedera.hashgraph.sdk.TransactionId;
import com.hedera.hashgraph.sdk.TransactionReceipt;
import com.hedera.hashgraph.sdk.account.AccountId;
import com.hedera.hashgraph.sdk.consensus.ConsensusTopicId;
import com.hedera.hashgraph.sdk.consensus.ConsensusTopicUpdateTransaction;
import com.hedera.hashgraph.sdk.crypto.PublicKey;
import com.hedera.hashgraph.sdk.crypto.ed25519.Ed25519PrivateKey;
import com.hedera.hcs.sxc.HCSCore;
    
public final class UpdateHCSTopic {
    private Map<AccountId, String> nodeMap = new HashMap<AccountId, String>();
    private AccountId operatorAccountId = new AccountId(0, 0, 0); 
    private Ed25519PrivateKey ed25519PrivateKey;
    private ConsensusTopicUpdateTransaction tx = new ConsensusTopicUpdateTransaction();
    
    public UpdateHCSTopic(HCSCore hcsCore) {
        this.nodeMap = hcsCore.getNodeMap();
        this.operatorAccountId = hcsCore.getOperatorAccountId();
        this.ed25519PrivateKey = hcsCore.getEd25519PrivateKey();
        this.tx.setMaxTransactionFee(hcsCore.getMaxTransactionFee());
    }
    
    public UpdateHCSTopic overrideNodeMap(Map<AccountId, String> nodeMap) {
        this.nodeMap = nodeMap;
        return this;
    }
    public UpdateHCSTopic overrideOperatorAccountId(AccountId operatorAccountId) {
        this.operatorAccountId = operatorAccountId;
        return this;
    }
    public UpdateHCSTopic overrideOperatorKey(Ed25519PrivateKey ed25519PrivateKey) {
        this.ed25519PrivateKey = ed25519PrivateKey;
        return this;
    }
    public UpdateHCSTopic setMaxTransactionFee(Long maxTansactionFee) {
        this.tx.setMaxTransactionFee(maxTansactionFee);
        return this;
    }
    public UpdateHCSTopic setTopicMemo (String topicMemo) {
        this.tx.setTopicMemo(topicMemo);
        return this;
    }
    public UpdateHCSTopic setSubmitKey(PublicKey submitKey) {
        this.tx.setSubmitKey(submitKey);
        return this;
    }
    public UpdateHCSTopic setAdminKey(PublicKey adminKey) {
        this.tx.setAdminKey(adminKey);
        return this;
    }
    public UpdateHCSTopic setAutoRenewAccountId(AccountId autoRenewAccountId) {
        this.tx.setAutoRenewAccountId(autoRenewAccountId);
        return this;
    }
    public UpdateHCSTopic setAutoRenewAccountId(String autoRenewAccountId) {
        this.tx.setAutoRenewAccountId(AccountId.fromString(autoRenewAccountId));
        return this;
    }
    public UpdateHCSTopic setAutoRenewPeriod(Duration autoRenewPeriod) {
        this.tx.setAutoRenewPeriod(autoRenewPeriod);
        return this;
    }
    public UpdateHCSTopic setExpirationTime(Instant expirationTime) {
        this.tx.setExpirationTime(expirationTime);
        return this;
    }
    public UpdateHCSTopic setTransactionMemo(String transactionMemo) {
        this.tx.setTransactionMemo(transactionMemo);
        return this;
    }
    public UpdateHCSTopic setTopicId(ConsensusTopicId topicId) {
        this.tx.setTopicId(topicId);
        return this;
    }
    public UpdateHCSTopic setTopicId(String topicId) {
        this.tx.setTopicId(ConsensusTopicId.fromString(topicId));
        return this;
    }

    public UpdateHCSTopic clearAdminKey() {
        this.tx.clearAdminKey();
        return this;
    }
    public UpdateHCSTopic clearAutoRenewAccountId() {
        this.tx.clearAutoRenewAccountId();
        return this;
    }
    
    public UpdateHCSTopic clearSubmitKey() {
        this.tx.clearSubmitKey();
        return this;
    }
    public UpdateHCSTopic clearTopicMemo() {
        this.tx.clearTopicMemo();
        return this;
    }
    
    /**
     * Updates a topic on a Hedera network
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

