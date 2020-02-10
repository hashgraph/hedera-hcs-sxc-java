package com.hedera.hcsapp.restclasses;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

public class AuditThreadIdTest {
    
    @Test
    public void testAuditThreadId() {
        AuditThreadId auditThreadId = new AuditThreadId("threadId", "context", "status", "topicId", "createdDate", "createdTime");
        assertEquals("threadId", auditThreadId.getThreadId());
        assertEquals("context", auditThreadId.getContext());
        assertEquals("status", auditThreadId.getStatus());
        assertEquals("topicId", auditThreadId.getTopicId());
        assertEquals("createdDate", auditThreadId.getCreatedDate());
        assertEquals("createdTime", auditThreadId.getCreatedTime());
    }
}
