package com.hedera.hcsapp.restclasses;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NonNull;

@Data
@AllArgsConstructor
public final class AuditThreadId implements Comparable<AuditThreadId> {
    
    @NonNull
    private String threadId;
    @NonNull
    private String context;
    @NonNull
    private String status;
    @NonNull
    private String topicId;
    @NonNull
    private String createdDate;
    @NonNull
    private String createdTime;

    // override equals and hashCode
    @Override
    public int compareTo(AuditThreadId auditThreadId) {
        return this.threadId.compareTo(auditThreadId.getThreadId());
    }
    
}
