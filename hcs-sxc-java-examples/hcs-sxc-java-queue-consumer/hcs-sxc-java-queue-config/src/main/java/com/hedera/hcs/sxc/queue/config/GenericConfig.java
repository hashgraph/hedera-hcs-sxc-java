package com.hedera.hcs.sxc.queue.config;


public class GenericConfig {
    private String exchangeName = "";
    private String consumerTag =  "";
    private String producerTag = "";
    private boolean enabled = false;
    private String topicId = "";
    
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
    public boolean getEnabled() {
        return this.enabled;
    }
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
    public String getTopicId() {
        return this.topicId;
    }
    public void setTopicId(String topicId) {
        this.topicId = topicId;
    }
}
