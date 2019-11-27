package com.hedera.hcsapp.restclasses;

import java.util.List;

import lombok.Data;

@Data
public final class AuditThreadIds {
    private List<String> threadIds;
}
