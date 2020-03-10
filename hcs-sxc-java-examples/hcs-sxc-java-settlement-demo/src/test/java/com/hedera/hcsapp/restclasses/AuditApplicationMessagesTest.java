package com.hedera.hcsapp.restclasses;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import com.hedera.hcsapp.AppData;

public class AuditApplicationMessagesTest {
    @Test
    public void testAuditApplicationMessages() throws Exception {
        AppData appData = new AppData( "./src/test/resources/config.yaml", "./src/test/resources/dotenv.sample", "./src/test/resources/docker-compose.yml","./src/test/resources/contact-list.yaml");

        AuditApplicationMessage auditApplicationMessage = new AuditApplicationMessage(appData);
        auditApplicationMessage.setApplicationMessageId("applicationMessageId");
        auditApplicationMessage.setMessage("message");

        AuditApplicationMessages auditApplicationMessages = new AuditApplicationMessages();
        auditApplicationMessages.getAuditApplicationMessages().add(auditApplicationMessage);
        
        assertEquals(1, auditApplicationMessages.getAuditApplicationMessages().size());
        AuditApplicationMessage auditApplicationMessageTest = auditApplicationMessages.getAuditApplicationMessages().get(0);
        
        assertEquals(auditApplicationMessage.getApplicationMessageId(), auditApplicationMessageTest.getApplicationMessageId());
        assertEquals(auditApplicationMessage.getMessage(), auditApplicationMessageTest.getMessage());
        assertEquals(auditApplicationMessage.getTopicId(), auditApplicationMessageTest.getTopicId());
        
    }
}
