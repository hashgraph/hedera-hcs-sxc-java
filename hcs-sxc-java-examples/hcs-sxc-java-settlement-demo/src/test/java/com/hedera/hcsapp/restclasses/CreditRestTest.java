package com.hedera.hcsapp.restclasses;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import com.hedera.hcsapp.AppData;
import com.hedera.hcsapp.States;
import com.hedera.hcsapp.entities.Credit;

public class CreditRestTest {
    @Test
    public void testCreditRest() throws Exception {
        AppData appData = new AppData("./src/test/resources/config.yaml", "./src/test/resources/dotenv.sample", "./src/test/resources/docker-compose.yml","./src/test/resources/contact-list.yaml");

        Credit credit = new Credit();
        credit.setAdditionalNotes("additionalNotes");
        credit.setAmount(100);
        credit.setApplicationMessageId("applicationMessageId");
        credit.setAutomatic(true);
        credit.setCreatedDate("createdDate");
        credit.setCreatedTime("createdTime");
        credit.setCurrency("currency");
        credit.setPayerName("payerName");
        credit.setRecipientName("recipientName");
        credit.setReference("reference");
        credit.setStatus(States.CREDIT_PROPOSED_PENDING.name());
        credit.setThreadId("threadId");
        credit.setAutomatic(true);
        assertTrue(credit.getAutomatic());
        
        CreditRest creditRest = new CreditRest(credit, appData);
        
        assertEquals(credit.getThreadId(),  creditRest.getThreadId());
        assertEquals(credit.getApplicationMessageId(),  creditRest.getApplicationMessageId());
        assertEquals(credit.getPayerName(),  creditRest.getPayerName());
        assertEquals(credit.getRecipientName(),  creditRest.getRecipientName());
        assertEquals(credit.getReference(),  creditRest.getReference());
        assertEquals(credit.getAmount(),  creditRest.getAmount());
        assertEquals(credit.getCurrency(),  creditRest.getCurrency());
        assertEquals(credit.getAdditionalNotes(),  creditRest.getAdditionalNotes());
        assertEquals(credit.getStatus(),  creditRest.getStatus());
        assertEquals(credit.getCreatedDate() + " " + credit.getCreatedTime(),  creditRest.getCreatedDateTime());
        assertEquals(appData.getHCSCore().getTopics().get(appData.getTopicIndex()).getTopic(),  creditRest.getTopicId());
        assertEquals(credit.getStatus(),  creditRest.getStatus());
        assertEquals(States.valueOf(credit.getStatus()).getDisplayForCredit(),  creditRest.getDisplayStatus());
    }
}
