package com.hedera.hcsapp.notifications;

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

public class NotificationMessage {
 
    private String recipient;
    private String payer;
    private String threadId;
    private String context;

    public String getRecipient() {
        return this.recipient;
    }
    public void setRecipient(String recipient) {
        this.recipient = recipient;
    }

    public String getPayer() {
        return this.payer;
    }
    public void setPayer(String payer) {
        this.payer = payer;
    }

    public String getThreadId() {
        return this.threadId;
    }
    public void setThreadId(String threadId) {
        this.threadId = threadId;
    }

    public String getContext() {
        return this.context;
    }
    public void setContext(String context) {
        this.context = context;
    }
    
}
