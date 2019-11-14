package com.hedera.hcsapp;

public final class Enums {
    public enum state {
        CREDIT_PENDING
        ,CREDIT_AWAIT_ACK
        ,CREDIT_ACK
        
        ,SETTLE_PROPOSE_PENDING
        ,SETTLE_PROPOSE_AWAIT_ACK
        ,SETTLE_PROPOSE_ACK

        ,SETTLE_INIT_PENDING
        ,SETTLE_INIT_AWAIT_ACK
        ,SETTLE_INIT_ACK

        ,PAYMENT_INIT_PENDING
        ,PAYMENT_INIT_AWAIT_ACK
        ,PAYMENT_INIT_ACK

        ,PAYMENT_SENT_PENDING
        ,PAYMENT_SENT_AWAIT_ACK
        ,PAYMENT_SENT_ACK

        ,SETTLE_PAID_PENDING
        ,SETTLE_PAID_AWAIT_ACK
        ,SETTLE_PAID_ACK

        ,SETTLE_COMPLETE_PENDING
        ,SETTLE_COMPLETE_AWAIT_ACK
        ,SETTLE_COMPLETE_ACK
    }
}
