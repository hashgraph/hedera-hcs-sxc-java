package com.hedera.hcslib.config;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.hedera.hashgraph.sdk.account.AccountId;

public final class YAMLConfig {

    private List<Node> nodes = new ArrayList<Node>();
    private AppNet appNet = new AppNet();
    private Long HCSTransactionFee = 0L;
    
    public List<Node> getNodes() {
        return this.nodes;
    }
    public void setNodes(List<Node> nodes) {
        this.nodes = nodes;
    }
    public AppNet getAppNet() {
        return this.appNet;
    }
    public void setAppNet(AppNet appNet) {
        this.appNet = appNet;
    }
    public long getHCSTransactionFee() {
        return this.HCSTransactionFee;
    }
    public void setHCSTransactionFee(long hcsTransactionFee) {
        this.HCSTransactionFee = hcsTransactionFee;
    }
    /** 
     * Returns a map of node details (AccountId and address)
     * @return Map<AccountId, String> 
     */
    public Map<AccountId, String> getNodesMap() {
        Map<AccountId, String> nodeList = new HashMap<AccountId, String>();
        
        for (Node node : this.nodes) {
            nodeList.put(AccountId.fromString(node.getAccount()), node.getAddress());
        }
        
        return nodeList;
    }
}