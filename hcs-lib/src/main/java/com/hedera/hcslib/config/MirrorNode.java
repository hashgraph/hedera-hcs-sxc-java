package com.hedera.hcslib.config;

public final class MirrorNode {
    private String address;
    private int reconnectDelay = 0;
    
    public int getReconnectDelay() {
        return this.reconnectDelay;
    }
    public void setReconnectDelay(int reconnectDelay) {
        this.reconnectDelay = reconnectDelay;
    }
    public String getAddress() {
        return this.address;
    }
    public void setAddress(String mirrorAddress) {
        this.address = mirrorAddress;
    }

}
