package com.hedera.hcs.sxc.plugin.mirror.config;

public final class YAMLConfig {

    private Queue queue = new Queue();
    public Queue getQueue() {
        return this.queue;
    }
    public void setQueue(Queue queue) {
        this.queue = queue;
    }
}