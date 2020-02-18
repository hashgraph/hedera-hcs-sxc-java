package com.hedera.hcsapp.restclasses;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import com.hedera.hcsapp.AppData;

public class AuditApplicationMessageTest {
    @Test
    public void testAuditApplicationMessage() throws Exception {
        AppData appData = new AppData("0", "./src/test/resources/config.yaml", "./src/test/resources/dotenv.sample", "./src/test/resources/docker-compose.yml");

        AuditApplicationMessage auditApplicationMessage = new AuditApplicationMessage(appData);
        auditApplicationMessage.setApplicationMessageId("applicationMessageId");
        auditApplicationMessage.setMessage("message");
        
        assertEquals("applicationMessageId", auditApplicationMessage.getApplicationMessageId());
        assertEquals("message", auditApplicationMessage.getMessage());
        assertEquals(appData.getHCSCore().getTopics().get(appData.getTopicIndex()).getTopic(), auditApplicationMessage.getTopicId());
        
    }
}
