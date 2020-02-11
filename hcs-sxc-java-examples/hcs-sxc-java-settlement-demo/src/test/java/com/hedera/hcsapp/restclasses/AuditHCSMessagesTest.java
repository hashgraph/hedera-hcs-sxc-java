package com.hedera.hcsapp.restclasses;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import com.hedera.hcsapp.AppData;

public class AuditHCSMessagesTest {
    @Test
    public void testAuditApplicationMessages() throws Exception {
        AppData appData = new AppData(0, "./src/test/resources/config.yaml", "./src/test/resources/dotenv.sample", "./src/test/resources/docker-compose.yml");

        AuditHCSMessage auditHCSMessage = new AuditHCSMessage(appData);
        auditHCSMessage.setConsensusTimeStampNanos(10);
        auditHCSMessage.setConsensusTimeStampSeconds(20);
        auditHCSMessage.setRunningHash("running hash");
        auditHCSMessage.setSequenceNumber(30);
        auditHCSMessage.setMessage("message");
        auditHCSMessage.setPart("1");
        auditHCSMessage.setTopicId("topicId");

        AuditHCSMessages auditHCSMessages = new AuditHCSMessages();
        auditHCSMessages.getAuditHCSMessages().add(auditHCSMessage);
        
        assertEquals(1, auditHCSMessages.getAuditHCSMessages().size());
        AuditHCSMessage auditHCSMessagesTest = auditHCSMessages.getAuditHCSMessages().get(0);
        
        assertEquals(auditHCSMessage.getConsensusTimeStampNanos(), auditHCSMessagesTest.getConsensusTimeStampNanos());
        assertEquals(auditHCSMessage.getConsensusTimeStampSeconds(), auditHCSMessagesTest.getConsensusTimeStampSeconds());
        assertEquals(auditHCSMessage.getRunningHash(), auditHCSMessagesTest.getRunningHash());
        assertEquals(auditHCSMessage.getSequenceNumber(), auditHCSMessagesTest.getSequenceNumber());
        assertEquals(auditHCSMessage.getMessage(),  auditHCSMessagesTest.getMessage());
        assertEquals(auditHCSMessage.getPart(), auditHCSMessagesTest.getPart());
        assertEquals(auditHCSMessage.getTopicId(), auditHCSMessagesTest.getTopicId());
        
    }
}


