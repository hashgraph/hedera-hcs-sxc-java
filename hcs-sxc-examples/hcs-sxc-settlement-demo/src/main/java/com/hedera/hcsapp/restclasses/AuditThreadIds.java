package com.hedera.hcsapp.restclasses;

import java.util.ArrayList;
import java.util.List;

import lombok.Data;

@Data
public final class AuditThreadIds {
    private List<AuditThreadId> threadIds = new ArrayList<AuditThreadId>();
}
