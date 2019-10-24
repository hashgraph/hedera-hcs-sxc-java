package com.hedera.demo.config;

import java.util.ArrayList;
import java.util.List;

public final class Config {
    private List<Node> nodes = new ArrayList<Node>();
    private AppNet appNet = new AppNet();
    private Relay relay = new Relay();
    private List<AppClient> appClients = new ArrayList<AppClient>();

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
    public Relay getRelay() {
        return this.relay;
    }
    public void setRelay(Relay relay) {
        this.relay = relay;
    }
    public List<AppClient> getAppClients() {
        return this.appClients;
    }
    public void setAppClients(List<AppClient> appClients) {
        this.appClients = appClients;
    }
}