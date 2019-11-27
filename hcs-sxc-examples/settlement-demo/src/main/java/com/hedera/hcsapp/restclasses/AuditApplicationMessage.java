package com.hedera.hcsapp.restclasses;

import lombok.Data;

@Data
public class AuditApplicationMessage {
    private String applicationMessageId;
    private String message;
}
