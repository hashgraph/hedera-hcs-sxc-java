package com.hedera.hcsapp.restclasses;

import java.util.ArrayList;
import java.util.List;

import com.hedera.hcsapp.AppData;
import com.hedera.hcsapp.States;
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
        this.topicId = appData.getHCSLib().getTopicIds().get(appData.getTopicIndex()).toString();
        this.displayStatus = States.valueOf(this.status).getDisplayForSettlement();
        this.paymentChannelName = settlement.getPaymentChannelName();
        this.payerAccountDetails = settlement.getPayerAccountDetails();
        this.recipientAccountDetails = settlement.getRecipientAccountDetails();
        this.paymentReference = settlement.getPaymentReference();
        
        List<SettlementItem> settlementItemsFromDB = settlementItemRepository.findAllSettlementItems(settlement.getThreadId());
        List<String> threadIds = new ArrayList<String>();
        for (SettlementItem settlementItem : settlementItemsFromDB) {
            this.threadIds.add(settlementItem.getId().getSettledThreadId());
            creditRepository.findById(settlementItem.getId().getSettledThreadId()).ifPresent(
                    (credit) -> {
                        credits.add(new CreditRest(credit, appData));        
                    }
            );
                
        }
    }
}
