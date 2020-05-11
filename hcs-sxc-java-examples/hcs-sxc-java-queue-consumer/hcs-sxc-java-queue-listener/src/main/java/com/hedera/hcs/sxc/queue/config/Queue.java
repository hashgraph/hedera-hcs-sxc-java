package com.hedera.hcs.sxc.queue.config;

public final class Queue {
    private String exchangeName = "";
    private String consumerTag =  "";
    private String producerTag = "";
    private String host = "";
    private String user = "";
    private String password = "";
    private int iterations = 0;
    private int delayMillis = 2;
    private int port = 5672;
    private String provider = "";
    
    public String getExchangeName() {
        return exchangeName;
    }

    public void setExchangeName(String exchangeName) {
        this.exchangeName = exchangeName;
    }

    public String getConsumerTag() {
        return consumerTag;
    }

    public void setConsumerTag(String consumerTag) {
        this.consumerTag = consumerTag;
    }

    public String getProducerTag() {
        return producerTag;
    }

    public void setProducerTag(String producerTag) {
        this.producerTag = producerTag;
    }

    public String getHost() {
        return this.host;
    }
    public void setHost(String host) {
        this.host = host;
    }
    public String getUser() {
        return this.user;
    }
    public void setUser(String user) {
        this.user = user;
    }
    public String getPassword() {
        return this.password;
    }
    public void setPassword(String password) {
        this.password = password;
    }
    public int getIterations() {
        return this.iterations;
    }
    public void setIterations(int iterations) {
        this.iterations = iterations;
    }
    public int getDelayMillis() {
        return this.delayMillis;
    }
    public void setDelayMillis(int delayMillis) { 
        this.delayMillis = delayMillis; 
    }
    public int getPort() { 
        return this.port;
    }
    public void setPort(int port) { 
        this.port = port; 
    }
    public String getProvider() {
        return this.provider;
    }
    public void setProvider(String provider) {
        this.provider = provider;
    }
}
