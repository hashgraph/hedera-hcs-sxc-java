package com.hedera.hcsapp.restclasses;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;

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
public class SettlementRestTest {    
    
    @MockBean
    private static SettlementItemRepository settlementItemRepository;
    
    @MockBean
    private static CreditRepository creditRepository;
    
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
        credit.get().setStatus("creditstatus");
        credit.get().setThreadId("creditthreadId");
        
        Mockito.when(creditRepository.findById("creditthreadId"))
            .thenReturn(credit);
        
        SettlementItemId settlementItemId = new SettlementItemId();
        settlementItemId.setSettledThreadId("creditthreadId");
        settlementItemId.setThreadId("threadid");
        
        SettlementItem settlementItem = new SettlementItem();
        settlementItem.setId(settlementItemId);
        List<SettlementItem> settlementItems = new ArrayList<SettlementItem>();
        settlementItems.add(settlementItem);
        
        Mockito.when(settlementItemRepository.findAllSettlementItems("threadid"))
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