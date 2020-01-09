package com.hedera.hcsapp.restclasses;

import java.util.ArrayList;
import java.util.List;

import lombok.Data;

@Data
public class AuditApplicationMessages {
    private List<AuditApplicationMessage> auditApplicationMessages = new ArrayList<AuditApplicationMessage>();
}
