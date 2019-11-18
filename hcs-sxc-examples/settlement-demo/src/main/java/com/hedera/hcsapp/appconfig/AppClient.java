package com.hedera.hcsapp.appconfig;

public class AppClient {
    private String clientName = "";
    private String clientKey = "";
    private String roles = "";
    
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
}
