package com.hedera.hcs.sxc.mq.generator.config;

public final class Queue {
    private String exchangeName = "";
    private String consumerTag =  "";
    private String producerTag = "";
    private String host = "";
    private String user = "";
    private String password = "";
    private int port = 0;
    private int iterations = 0;
    private int delayMillis = 2;


    public String getExchangeName() {
        return exchangeName;
    }

    public void setExchangeName(String echangeName) {
        this.exchangeName = echangeName;
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
    public int getPort() {
        return this.port;
    }
    public void setPort(int port) {
        this.port = port;
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
    public void setDelayMillis(int delayMillis) { this.delayMillis = delayMillis; }
}
