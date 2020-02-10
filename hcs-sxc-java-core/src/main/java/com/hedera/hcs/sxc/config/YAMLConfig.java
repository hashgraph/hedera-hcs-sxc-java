package com.hedera.hcs.sxc.config;

/*-
 * ‌
 * hcs-sxc-java
 * ​
 * Copyright (C) 2019 - 2020 Hedera Hashgraph, LLC
 * ​
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ‍
 */

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.hedera.hashgraph.sdk.account.AccountId;

public final class YAMLConfig {

    private List<Node> nodes = new ArrayList<Node>();
    private AppNet appNet = new AppNet();
    private Long HCSTransactionFee = 0L;
    private MirrorNode mirrorNode = new MirrorNode();
    private Map<String, String> coreHibernate = new HashMap<String, String>();

    public MirrorNode getMirrorNode() {
        return this.mirrorNode;
    }
    public void setMirrorNode(MirrorNode mirrorNode) {
        this.mirrorNode = mirrorNode;
    }
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
    public Map<String, String> getCoreHibernate() {
        return this.coreHibernate;
    }
    public void setCoreHibernate(Map<String, String> coreHibernate) {
        this.coreHibernate = coreHibernate;
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
