package com.hedera.hcsapp.restclasses;

import com.hedera.hcsapp.AppData;

import lombok.Data;

@Data
public class AuditApplicationMessage {
    private String applicationMessageId;
    private String message;
    private String topicId;
    
    public AuditApplicationMessage(AppData appData) {
        this.topicId = appData.getHCSCore().getTopicIds().get(appData.getTopicIndex()).toString();
    }
}
