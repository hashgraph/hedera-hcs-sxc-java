package com.hedera.hcsapp.restclasses;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

public class AuditThreadIdsTest {
    
    @Test
    public void testAuditThreadIds() {
        AuditThreadId auditThreadId = new AuditThreadId("threadId", "context", "status", "topicId", "createdDate", "createdTime");
        AuditThreadIds auditThreadIds = new AuditThreadIds();
        
        auditThreadIds.getThreadIds().add(auditThreadId);
        
        assertEquals(1, auditThreadIds.getThreadIds().size());
        AuditThreadId auditThreadIdTest = auditThreadIds.getThreadIds().get(0);

        assertEquals(auditThreadId.getThreadId(), auditThreadIdTest.getThreadId());
        assertEquals(auditThreadId.getContext(), auditThreadIdTest.getContext());
        assertEquals(auditThreadId.getStatus(), auditThreadIdTest.getStatus());
        assertEquals(auditThreadId.getTopicId(), auditThreadIdTest.getTopicId());
        assertEquals(auditThreadId.getCreatedDate(), auditThreadIdTest.getCreatedDate());
        assertEquals(auditThreadId.getCreatedTime(), auditThreadIdTest.getCreatedTime());
        
    }
}
