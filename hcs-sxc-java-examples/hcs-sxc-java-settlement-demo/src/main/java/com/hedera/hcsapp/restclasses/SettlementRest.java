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

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.hedera.hcsapp.AppData;
import com.hedera.hcsapp.States;
import com.hedera.hcsapp.entities.Credit;
import com.hedera.hcsapp.entities.Settlement;
import com.hedera.hcsapp.entities.SettlementItem;
import com.hedera.hcsapp.repository.CreditRepository;
import com.hedera.hcsapp.repository.SettlementItemRepository;

import lombok.Data;

@Data
public final class SettlementRest {

    private String threadId;
    private String applicationMessageId;
    private String payerName;
    private String recipientName;
    private String additionalNotes;
    private long netValue;
    private String currency;
    private String status;
    private String createdDateTime;
    private String topicId;
    private String displayStatus;
    private String paymentChannelName;
    private String payerAccountDetails;
    private String recipientAccountDetails;
    private String paymentReference;

    private List<CreditRest> credits = new ArrayList<CreditRest>();
    private List<String> threadIds = new ArrayList<String>();

    public SettlementRest (Settlement settlement, AppData appData, SettlementItemRepository settlementItemRepository, CreditRepository creditRepository) {
        this.threadId = settlement.getThreadId();
        this.applicationMessageId = settlement.getApplicationMessageId();
        this.payerName = settlement.getPayerName();
        this.recipientName = settlement.getRecipientName();
        this.additionalNotes = settlement.getAdditionalNotes();
        this.netValue = settlement.getNetValue();
        this.currency = settlement.getCurrency();
        this.status = settlement.getStatus();
        this.createdDateTime = settlement.getCreatedDate() + " " + settlement.getCreatedTime();
        this.topicId = appData.getHCSCore().getTopics().get(appData.getTopicIndex()).getTopic();
        this.displayStatus = States.valueOf(this.status).getDisplayForSettlement();
        this.paymentChannelName = settlement.getPaymentChannelName();
        this.payerAccountDetails = settlement.getPayerAccountDetails();
        this.recipientAccountDetails = settlement.getRecipientAccountDetails();
        this.paymentReference = settlement.getPaymentReference();

        List<SettlementItem> settlementItemsFromDB = settlementItemRepository.findAllSettlementItems(settlement.getThreadId());
        for (SettlementItem settlementItem : settlementItemsFromDB) {
            this.threadIds.add(settlementItem.getId().getSettledThreadId());
            Optional<Credit> credit = creditRepository.findById(settlementItem.getId().getSettledThreadId());
            if (credit.isPresent()) {
                credits.add(new CreditRest(credit.get(), appData));
            }
        }
    }
}
