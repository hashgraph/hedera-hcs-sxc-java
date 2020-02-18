package com.hedera.hcsapp.appconfig;

/*-
 * ‌
 * hcs-sxc-java
 * ​
 * Copyright (C) 2019 - 2020 Hedera Hashgraph, LLC
 * ​
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ‍
 */

public class AppClient {
    private String clientName = "";
    private String clientKey = "";
    private String roles = "";
    private String paymentAccountDetails = "";
    private String color = "";
    private String appId = "";
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
    public String getAppId() {
        return this.appId;
    }
    public void setAppId(String appId) {
        this.appId = appId;
    }
    public int getWebPort() {
        return this.webPort;
    }
    public void setWebPort(int port) {
        this.webPort = port;
    }
}
