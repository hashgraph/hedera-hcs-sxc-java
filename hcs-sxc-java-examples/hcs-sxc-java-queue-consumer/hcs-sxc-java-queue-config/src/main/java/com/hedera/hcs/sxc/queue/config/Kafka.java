package com.hedera.hcs.sxc.queue.config;


public final class Kafka extends GenericConfig {
    private String host = "";
    private int port = 9092;

    public String getHost() {
        return this.host;
    }
    public void setHost(String host) {
        this.host = host;
    }
    public int getPort() { 
        return this.port;
    }
    public void setPort(int port) { 
        this.port = port; 
    }
}
