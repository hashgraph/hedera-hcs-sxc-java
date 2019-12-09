package com.hedera.hcsapp;

public enum States {
    CREDIT_PROPOSED_PENDING("Credit Proposed (Pending)")
    ,CREDIT_PROPOSED("Credit Proposed")
    ,CREDIT_AGREED_PENDING("Credit Agreed (Pending)")
    ,CREDIT_AGREED("Credit Agreed")
    
    ,SETTLEMENT_PROPOSED_PENDING("Settlement Proposed (Pending)")
    ,SETTLEMENT_PROPOSED("Settlement Proposed")
    ,SETTLEMENT_AGREED_PENDING("Settlement Agreed (Pending)")
    ,SETTLEMENT_AGREED("Settlement Agreed")

    ,SETTLE_INIT_PENDING("Settlement Channel Proposed (Pending)")
    ,SETTLE_INIT_AWAIT_ACK("Settlement Channel Proposed")
    ,SETTLE_INIT_ACK_PENDING("Settlement Agreed (Pending)")
    ,SETTLE_INIT_ACK("Settlement Channel Agreed")

    ,PAYMENT_INIT_PENDING("PAYMENT_INIT_PENDING")
    ,PAYMENT_INIT_AWAIT_ACK("PAYMENT_INIT_AWAIT_ACK")
    ,PAYMENT_INIT_ACK("PAYMENT_INIT_ACK")

    ,PAYMENT_SENT_PENDING("PAYMENT_SENT_PENDING")
    ,PAYMENT_SENT_AWAIT_ACK("PAYMENT_SENT_AWAIT_ACK")
    ,PAYMENT_SENT_ACK("PAYMENT_SENT_ACK")

    ,SETTLE_PAID_PENDING("SETTLE_PAID_PENDING")
    ,SETTLE_PAID_AWAIT_ACK("SETTLE_PAID_AWAIT_ACK")
    ,SETTLE_PAID_ACK("SETTLE_PAID_ACK")

    ,SETTLE_COMPLETE_PENDING("SETTLE_COMPLETE_PENDING")
    ,SETTLE_COMPLETE_AWAIT_ACK("SETTLE_COMPLETE_AWAIT_ACK")
    ,SETTLE_COMPLETE_ACK("SETTLE_COMPLETE_ACK");
    
    private final String display;
    
    States(String display) {
        this.display = display;
    }
    
    public String getDisplay() {
        return this.display;
    }
}

