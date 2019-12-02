package com.hedera.hcsapp.restclasses;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;

import com.hedera.hcsapp.AppData;
import com.hedera.hcsapp.entities.Credit;
import com.hedera.hcsapp.entities.Settlement;
import com.hedera.hcsapp.entities.SettlementItem;
import com.hedera.hcsapp.repository.CreditRepository;
import com.hedera.hcsapp.repository.SettlementItemRepository;

import lombok.Data;

@Data
public final class SettlementRest {
    
    @Autowired
    SettlementItemRepository settlementItemRepository;

    @Autowired
    CreditRepository creditRepository;

    private String threadId;
    private String applicationMessageId;
    private String payerName;
    private String recipientName;
    private String additionalNotes;
    private long netValue;
    private String currency;
    private String status;
    private String createdDate;
    private String createdTime;
    private String topicId;
    
    private List<CreditRest> credits = new ArrayList<CreditRest>();
    private List<String> threadIds = new ArrayList<String>();
    
    public SettlementRest(Settlement settlement, AppData appData) {
        this.threadId = settlement.getThreadId();
        this.applicationMessageId = settlement.getApplicationMessageId();
        this.payerName = settlement.getPayerName();
        this.recipientName = settlement.getRecipientName();
        this.additionalNotes = settlement.getAdditionalNotes();
        this.netValue = settlement.getNetValue();
        this.currency = settlement.getCurrency();
        this.status = settlement.getStatus();
        this.createdDate = settlement.getCreatedDate();
        this.createdTime = settlement.getCreatedTime();
        this.topicId = appData.getHCSLib().getTopicIds().get(appData.getTopicIndex()).toString();

        List<SettlementItem> settlementItemsFromDB = settlementItemRepository.findAllSettlementItems(settlement.getThreadId());
        List<String> threadIds = new ArrayList<String>();
        for (SettlementItem settlementItem : settlementItemsFromDB) {
            threadIds.add(settlementItem.getId().getSettledThreadId());
            creditRepository.findById(settlementItem.getId().getSettledThreadId()).ifPresent(
                    (credit) -> {
                        credits.add(new CreditRest(credit, appData));        
                    }
            );
                
        }
    }
}
