package com.hedera.hcsapp.restclasses;


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;

import com.hedera.hcsapp.AppData;
import com.hedera.hcsapp.States;
import com.hedera.hcsapp.entities.Credit;
import com.hedera.hcsapp.entities.Settlement;
import com.hedera.hcsapp.entities.SettlementItem;
import com.hedera.hcsapp.entities.SettlementItemId;
import com.hedera.hcsapp.repository.CreditRepository;
import com.hedera.hcsapp.repository.SettlementItemRepository;
import com.hedera.hcsapp.restclasses.SettlementRestTest;

@ExtendWith(SpringExtension.class)
//@TestInstance(Lifecycle.PER_CLASS)
public class SettlementRestTest {    
    
    @MockBean
    private SettlementItemRepository settlementItemRepository;
    
    @MockBean
    private CreditRepository creditRepository;
    
    @BeforeEach
    public void setup(){
        MockitoAnnotations.initMocks(this); //without this you will get NPE
    }
    
    @Test
    public void testSettlementRest() throws Exception {
        Optional<Credit> credit = Optional.of(new Credit());
        credit.get().setAdditionalNotes("creditadditionalNotes");
        credit.get().setAmount(300);
        credit.get().setApplicationMessageId("creditapplicationMessageId");
        credit.get().setAutomatic(true);
        credit.get().setCreatedDate("creditcreatedDate");
        credit.get().setCreatedTime("creditcreatedTime");
        credit.get().setCurrency("creditcurrency");
        credit.get().setPayerName("creditpayerName");
        credit.get().setRecipientName("creditrecipientName");
        credit.get().setReference("creditreference");
        credit.get().setStatus(States.CREDIT_AGREED.name());
        credit.get().setThreadId("creditThreadId");

        Mockito.when(creditRepository.findById("creditThreadId"))
            .thenReturn(credit);
        
        SettlementItemId settlementItemId = new SettlementItemId();
        settlementItemId.setSettledThreadId("creditThreadId");
        settlementItemId.setThreadId("threadId");
        
        SettlementItem settlementItem = new SettlementItem();
        settlementItem.setId(settlementItemId);
        List<SettlementItem> settlementItems = new ArrayList<SettlementItem>();
        settlementItems.add(settlementItem);
        
        Mockito.when(settlementItemRepository.findAllSettlementItems("threadId"))
            .thenReturn(settlementItems);

        AppData appData = new AppData("./src/test/resources/config.yaml", "./src/test/resources/dotenv.sample", "./src/test/resources/docker-compose.yml");
        
        Settlement settlement = new Settlement();
        settlement.setAdditionalNotes("additionalNotes");
        settlement.setApplicationMessageId("applicationMessageId");
        settlement.setAutomatic(true);
        settlement.setCreatedDate("createdDate");
        settlement.setCreatedTime("createdTime");
        settlement.setCurrency("currency");
        settlement.setNetValue(200);
        settlement.setPayerName("payerName");
        settlement.setPayerAccountDetails("payerAccountDetails");
        settlement.setPaymentReference("paymentReference");
        settlement.setPaymentChannelName("paymentChannelName");
        settlement.setRecipientAccountDetails("recipientAccountDetails");
        settlement.setRecipientName("recipientName");
        settlement.setStatus(States.CREDIT_AGREED.name());
        settlement.setThreadId("threadId");
        settlement.setAutomatic(true);
        
        assertTrue(settlement.getAutomatic());
        
        SettlementRest settlementRest = new SettlementRest(settlement, appData, settlementItemRepository, creditRepository);

        assertEquals("threadId", settlementRest.getThreadId());
        assertEquals("applicationMessageId", settlementRest.getApplicationMessageId());
        assertEquals("payerName", settlementRest.getPayerName());
        assertEquals("recipientName", settlementRest.getRecipientName());
        assertEquals("additionalNotes", settlementRest.getAdditionalNotes());
        assertEquals(200, settlementRest.getNetValue());
        assertEquals("currency", settlementRest.getCurrency());
        assertEquals(States.CREDIT_AGREED.name(), settlementRest.getStatus());
        assertEquals(settlement.getCreatedDate() + " " + settlement.getCreatedTime(), settlementRest.getCreatedDateTime());
        assertEquals("0.0.162323", settlementRest.getTopicId());
        assertEquals(States.CREDIT_AGREED.getDisplayForSettlement(), settlementRest.getDisplayStatus());
        assertEquals("paymentChannelName", settlementRest.getPaymentChannelName());
        assertEquals("payerAccountDetails", settlementRest.getPayerAccountDetails());
        assertEquals("recipientAccountDetails", settlementRest.getRecipientAccountDetails());
        assertEquals("paymentReference", settlementRest.getPaymentReference());


//        private List<CreditRest> credits = new ArrayList<CreditRest>();
//        private List<String> threadIds = new ArrayList<String>();
        
    }

}
