package com.hedera.hcsapp.appconfig;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import com.hedera.hcs.sxc.utils.StringUtils;
import com.hedera.hcsapp.appconfig.AppClient;

public class AppClientTest {    
    
    @Test
    public void testAppClient() throws Exception {
        AppClient appClient = new AppClient();
        
        appClient.setClientName("clientName");
        appClient.setClientKey("clientKey");
        appClient.setAppId("appId");
        appClient.setColor("color");
        appClient.setPaymentAccountDetails("paymentAccountDetails");
        appClient.setRoles("roles");
        appClient.setWebPort(20);

        assertEquals("clientName", appClient.getClientName());
        assertEquals("clientKey", appClient.getClientKey());
        assertEquals("appId", appClient.getAppId());
        assertEquals("color", appClient.getColor());
        assertEquals("paymentAccountDetails", appClient.getPaymentAccountDetails());
        assertEquals("roles", appClient.getRoles());
        assertEquals(20, appClient.getWebPort());
    }
}
