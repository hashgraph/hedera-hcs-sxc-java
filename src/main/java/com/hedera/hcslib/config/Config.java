package com.hedera.hcslib.config;

import java.util.ArrayList;
import java.util.List;

public final class Config {
    private List<Node> nodes = new ArrayList<Node>();
    
    public List<Node> getNodes() {
        return this.nodes;
    }
    
    public void setNodes(List<Node> nodes) {
        this.nodes = nodes;
    }
}
