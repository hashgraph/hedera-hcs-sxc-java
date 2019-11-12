package com.hedera.hcsapp;

import java.util.ArrayList;
import java.util.List;

public final class AppYAML {

    private List<AppClient> appClients = new ArrayList<AppClient>();
    
    public List<AppClient> getAppClients() {
        return this.appClients;
    }
    public void setAppClients(List<AppClient> appClients) {
        this.appClients = appClients;
    }
}