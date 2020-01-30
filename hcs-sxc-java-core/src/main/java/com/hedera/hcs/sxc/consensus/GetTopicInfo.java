package com.hedera.hcs.sxc.consensus;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import com.hedera.hashgraph.sdk.Client;
import com.hedera.hashgraph.sdk.HederaNetworkException;
import com.hedera.hashgraph.sdk.HederaStatusException;
import com.hedera.hashgraph.sdk.account.AccountId;
import com.hedera.hashgraph.sdk.consensus.ConsensusTopicId;
import com.hedera.hashgraph.sdk.consensus.ConsensusTopicInfo;
import com.hedera.hashgraph.sdk.consensus.ConsensusTopicInfoQuery;
import com.hedera.hashgraph.sdk.crypto.ed25519.Ed25519PrivateKey;
import com.hedera.hcs.sxc.HCSCore;
    
public final class GetTopicInfo {
    private Map<AccountId, String> nodeMap = new HashMap<AccountId, String>();
    private AccountId operatorAccountId = new AccountId(0, 0, 0); 
    private Ed25519PrivateKey ed25519PrivateKey;
    private ConsensusTopicInfoQuery query = new ConsensusTopicInfoQuery();
    
    public GetTopicInfo(HCSCore hcsCore) {
        this.nodeMap = hcsCore.getNodeMap();
        this.operatorAccountId = hcsCore.getOperatorAccountId();
        this.ed25519PrivateKey = hcsCore.getEd25519PrivateKey();
    }    
    public GetTopicInfo overrideNodeMap(Map<AccountId, String> nodeMap) {
        this.nodeMap = nodeMap;
        return this;
    }
    public GetTopicInfo overrideOperatorAccountId(AccountId operatorAccountId) {
        this.operatorAccountId = operatorAccountId;
        return this;
    }
    public GetTopicInfo overrideOperatorKey(Ed25519PrivateKey ed25519PrivateKey) {
        this.ed25519PrivateKey = ed25519PrivateKey;
        return this;
    }

    public GetTopicInfo setMaxQueryPayment(long maxQueryPayment) {
        this.query.setMaxQueryPayment(maxQueryPayment);
        return this;
    }

    public GetTopicInfo setTopicId(ConsensusTopicId topicId) {
        this.query.setTopicId(topicId);
        return this;
    }
    public GetTopicInfo setTopicId(String topicId) {
        this.query.setTopicId(ConsensusTopicId.fromString(topicId));
        return this;
    }

    public ConsensusTopicInfo execute() throws HederaNetworkException, IllegalArgumentException, FileNotFoundException, IOException, HederaStatusException {
    
        Client client = new Client(this.nodeMap);
        client.setOperator(
            this.operatorAccountId
            ,this.ed25519PrivateKey
        );
        
        return query.execute(client);
    
    }
}

