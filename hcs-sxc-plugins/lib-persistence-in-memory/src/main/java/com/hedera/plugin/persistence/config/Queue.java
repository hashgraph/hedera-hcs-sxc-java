package com.hedera.plugin.persistence.config;

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

    public String getTcpConnectionFactory() {
        return this.tcpConnectionFactory;
    }
    public void setTcpConnectionFactory(String tcpConnectionFactory) {
        this.tcpConnectionFactory = tcpConnectionFactory;
    }
      
    
}
