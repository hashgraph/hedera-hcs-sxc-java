package com.hedera.hcsapp.restclasses;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import com.hedera.hcsapp.AppData;

public class AuditHCSMessageTest {
    @Test
    public void testAuditHCSMessage() throws Exception {
        AppData appData = new AppData("./src/test/resources/config.yaml", "./src/test/resources/dotenv.sample", "./src/test/resources/docker-compose.yml");

        AuditHCSMessage auditHCSMessage = new AuditHCSMessage(appData);
        assertEquals(appData.getHCSCore().getTopics().get(appData.getTopicIndex()).getTopic(), auditHCSMessage.getTopicId());
        
        auditHCSMessage.setConsensusTimeStampNanos(10);
        auditHCSMessage.setConsensusTimeStampSeconds(20);
        auditHCSMessage.setRunningHash("running hash");
        auditHCSMessage.setSequenceNumber(30);
        auditHCSMessage.setMessage("message");
        auditHCSMessage.setPart("1");
        auditHCSMessage.setTopicId("topicId");
        
        assertEquals(10, auditHCSMessage.getConsensusTimeStampNanos());
        assertEquals(20, auditHCSMessage.getConsensusTimeStampSeconds());
        assertEquals("running hash", auditHCSMessage.getRunningHash());
        assertEquals(30, auditHCSMessage.getSequenceNumber());
        assertEquals("message",  auditHCSMessage.getMessage());
        assertEquals("1", auditHCSMessage.getPart());
        assertEquals("topicId", auditHCSMessage.getTopicId());
    }
}
