package com.hedera.hcsapp.restclasses;


import lombok.Data;

@Data
public final class AuditThreadId implements Comparable<AuditThreadId> {
    
    private String threadId;
    private String context;

    public AuditThreadId(String threadId, String context) {
        this.threadId = threadId;
        this.context = context;
    }
    
    // override equals and hashCode
    @Override
    public int compareTo(AuditThreadId auditThreadId) {
        return this.threadId.compareTo(auditThreadId.getThreadId());
    }
    
}
