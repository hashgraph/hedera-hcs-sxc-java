package com.hedera.hcsapp.restclasses;

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

import com.hedera.hcsapp.AppData;
import com.hedera.hcsapp.States;
import com.hedera.hcsapp.entities.Credit;
import lombok.Data;

@Data
public final class CreditRest {

    private String threadId;
    private String applicationMessageId;
    private String payerName;
    private String recipientName;
    private String reference;
    private long amount;
    private String currency;
    private String additionalNotes;
    private String status;
    private String displayStatus;
    private String createdDateTime;
    private String topicId;

    public CreditRest(Credit credit, AppData appData) {
        this.threadId = credit.getThreadId();
        this.applicationMessageId = credit.getApplicationMessageId();
        this.payerName = credit.getPayerName();
        this.recipientName = credit.getRecipientName();
        this.reference = credit.getReference();
        this.amount = credit.getAmount();
        this.currency = credit.getCurrency();
        this.additionalNotes = credit.getAdditionalNotes();
        this.status = credit.getStatus();
        this.createdDateTime = credit.getCreatedDate() + " " + credit.getCreatedTime();
        this.topicId = appData.getHCSCore().getTopics().get(appData.getTopicIndex()).getTopic();
        this.displayStatus = States.valueOf(this.status).getDisplayForCredit();
    }
}
