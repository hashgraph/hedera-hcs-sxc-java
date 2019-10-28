package com.hedera.hcsrelay.config;

public final class Queue {
    private String initialContextFactory = "";
    private String topic = "";
    private String vmConnectionFactory = "";
    private String tcpConnectionFactory = "";
    private String JGroupsConnectionFactory = "";
    
    public String getInitialContextFactory() {
        return this.initialContextFactory;
    }
    public void setInitialContextFactory(String contextFactory) {
        this.initialContextFactory = contextFactory;
    }
    public String getTopic() {
        return this.topic;
    }
    public void setTopic(String topic) {
        this.topic = topic;
    }
    public String getVmConnectionFactory() {
        return this.vmConnectionFactory;
    }
    public void setVmConnectionFactory(String vmConnectionFactory) {
        this.vmConnectionFactory = vmConnectionFactory;
    }
    public String getTcpConnectionFactory() {
        return this.tcpConnectionFactory;
    }
    public void setTcpConnectionFactory(String tcpConnectionFactory) {
        this.tcpConnectionFactory = tcpConnectionFactory;
    }
    public String getJGroupsConnectionFactory() {
        return this.JGroupsConnectionFactory;
    }
    public void setJGroupsConnectionFactory(String jGroupsConnectionFactory) {
        this.JGroupsConnectionFactory = jGroupsConnectionFactory;
    }
}
