package com.hedera.hcs.sxc.interfaces;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import com.hedera.hcs.sxc.commonobjects.HCSResponse;
import com.hedera.hcs.sxc.proto.AccountID;
import com.hedera.hcs.sxc.proto.ApplicationMessageID;
import com.hedera.hcs.sxc.proto.Timestamp;

public class HCSResponseTest {

    @Test
    public void testHCSResponse() {
        HCSResponse hcsResponse = new HCSResponse();
        AccountID accountID = AccountID.newBuilder()
                .setShardNum(1)
                .setRealmNum(2)
                .setAccountNum(3)
                .build();
        Timestamp timestamp = Timestamp.newBuilder()
                .setSeconds(100)
                .setNanos(10)
                .build();
        ApplicationMessageID applicationMessageId = ApplicationMessageID.newBuilder()
                .setAccountID(accountID)
                .setValidStart(timestamp)
                .build();
        
        hcsResponse.setApplicationMessageID(applicationMessageId);
        hcsResponse.setMessage("testmessage".getBytes());
        
        assertEquals(accountID.getShardNum(), hcsResponse.getApplicationMessageId().getAccountID().getShardNum());
        assertEquals(accountID.getRealmNum(), hcsResponse.getApplicationMessageId().getAccountID().getRealmNum());
        assertEquals(accountID.getAccountNum(), hcsResponse.getApplicationMessageId().getAccountID().getAccountNum());
        assertEquals(timestamp.getSeconds(), hcsResponse.getApplicationMessageId().getValidStart().getSeconds());
        assertEquals(timestamp.getNanos(), hcsResponse.getApplicationMessageId().getValidStart().getNanos());
        assertArrayEquals("testmessage".getBytes(), hcsResponse.getMessage());
    }
}
