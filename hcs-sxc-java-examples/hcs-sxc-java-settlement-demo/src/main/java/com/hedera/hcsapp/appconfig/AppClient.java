package com.hedera.hcsapp.appconfig;

public class AppClient {
    private String clientName = "";
    private String clientKey = "";
    private String roles = "";
    private String paymentAccountDetails = "";
    private String color = "";
    private int appId = 0;
    private int webPort = 8080;
    
    public String getClientName() {
        return this.clientName;
    }
    public void setClientName(String clientName) {
        this.clientName = clientName;
    }
    public String getClientKey() {
        return this.clientKey;
    }
    public void setClientKey(String clientKey) {
        this.clientKey = clientKey;
    }
    public String getRoles() {
        return this.roles;
    }
    public void setRoles(String roles) {
        this.roles = roles;
    }
    public String getPaymentAccountDetails() {
        return this.paymentAccountDetails;
    }
    public void setPaymentAccountDetails(String paymentAccountDetails) {
        this.paymentAccountDetails = paymentAccountDetails;
    }
    public String getColor() {
        return this.color;
    }
    public void setColor(String color) {
        this.color = color;
    }
    public int getAppId() {
        return this.appId;
    }
    public void setAppId(int appId) {
        this.appId = appId;
    }
    public int getWebPort() {
        return this.webPort;
    }
    public void setWebPort(int port) {
        this.webPort = port;
    }
}
