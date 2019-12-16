package com.hedera.hcsapp;

public enum States {
    CREDIT_PROPOSED_PENDING("Credit Proposed (Pending)")
    ,CREDIT_PROPOSED("Credit Proposed")
    ,CREDIT_AGREED_PENDING("Credit Agreed (Pending)")
    ,CREDIT_AGREED("Credit Agreed")
    
    ,SETTLEMENT_PROPOSED_PENDING("Credit Settlement Proposed (Pending)")
    ,SETTLEMENT_PROPOSED("Credit Settlement Proposed")
    ,SETTLEMENT_AGREED_PENDING("Credit Settlement Agreed (Pending)")
    ,SETTLEMENT_AGREED("Credit Settlement Agreed")

    ,SETTLE_INIT_AWAIT_ACK_PENDING("Credit Settlement Payment Channel Proposed (Pending)")
    ,SETTLE_INIT_AWAIT_ACK("Credit Settlement Payment Channel Proposed")
    ,SETTLE_INIT_ACK_PENDING("Credit Settlement Payment Channel Agreed (Pending)")
    ,SETTLE_INIT_ACK("Credit Settlement Payment Channel Agreed")

    ,PAYMENT_INIT_AWAIT_ACK_PENDING("Settlement Payment Channel Proposed (Pending)")
    ,PAYMENT_INIT_AWAIT_ACK("Settlement Payment Channel Proposed")
    ,PAYMENT_INIT_ACK_PENDING("Settlement Payment Channel Agreed (Pending)")
    ,PAYMENT_INIT_ACK("Settlement Payment Channel Agreed")

    ,PAYMENT_SENT_AWAIT_ACK_PENDING("Settlement Payment Proposed (Pending)")
    ,PAYMENT_SENT_AWAIT_ACK("Settlement Payment Proposed")
    ,PAYMENT_SENT_ACK_PENDING("Settlement Payment Agreed (Pending)")
    ,PAYMENT_SENT_ACK("Settlement Payment Agreed")

    ,SETTLE_PAID_AWAIT_ACK_PENDING("Settlement Payment Made (Pending)")
    ,SETTLE_PAID_AWAIT_ACK("Settlement Payment Made")
    ,SETTLE_PAID_ACK_PENDING("Settlement Payment Acknowldegded (Pending)")
    ,SETTLE_PAID_ACK("Settlement Payment Acknowldegded")

    ,SETTLE_COMP_AWAIT_ACK_PENDING("Settlement Receipt Requested (Pending)")
    ,SETTLE_COMP_AWAIT_ACK("Settlement Receipt Requested")
    ,SETTLE_COMPLETE_ACK_PENDING("Settlement Receipt Confirmed (Pending)")
    ,SETTLE_COMPLETE_ACK("Settlement Receipt Confirmed");
    
    private final String display;
    
    States(String display) {
        this.display = display;
    }
    
    public String getDisplay() {
        return this.display;
    }
}

