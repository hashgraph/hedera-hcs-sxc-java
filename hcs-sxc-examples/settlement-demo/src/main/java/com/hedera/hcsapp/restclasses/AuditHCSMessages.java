package com.hedera.hcsapp.restclasses;

import java.util.ArrayList;
import java.util.List;

import lombok.Data;

@Data
public class AuditHCSMessages {
    private List<AuditHCSMessage> auditHCSMessages = new ArrayList<AuditHCSMessage>();
}
