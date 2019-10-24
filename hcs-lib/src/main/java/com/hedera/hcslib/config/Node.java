package com.hedera.hcslib.config;

public class Node {
    private String address = "";
    private String account = "";

    public void setAddress(String address) {
        this.address = address;
    }
    public String getAddress() {
        return this.address;
    }
    public void setAccount(String account) {
        this.account = account;
    }
    public String getAccount() {
        return this.account;
    }
}

