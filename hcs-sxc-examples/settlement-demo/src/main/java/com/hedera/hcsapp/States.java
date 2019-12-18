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

    ,SETTLE_INIT_AWAIT_ACK_PENDING("Settlement Payment Channel Proposed (Pending)")
    ,SETTLE_INIT_AWAIT_ACK("Settlement Payment Channel Proposed")
    ,SETTLE_INIT_ACK_PENDING("Settlement Payment Channel Agreed (Pending)")
    ,SETTLE_INIT_ACK("Settlement Payment Channel Agreed")

    ,PAYMENT_INIT_AWAIT_ACK_PENDING("Payment Proposed (Pending)")
    ,PAYMENT_INIT_AWAIT_ACK("Payment Proposed")
    ,PAYMENT_INIT_ACK_PENDING("Payment Agreed (Pending)")
    ,PAYMENT_INIT_ACK("Payment Agreed")

    ,PAYMENT_SENT_AWAIT_ACK_PENDING("Payment Sent (Pending)")
    ,PAYMENT_SENT_AWAIT_ACK("Payment Sent")
    ,PAYMENT_SENT_ACK_PENDING("Payment Sent Agreed (Pending)")
    ,PAYMENT_SENT_ACK("Payment Sent Agreed")
    //==
    ,SETTLE_PAID_AWAIT_ACK_PENDING("Settlement Payment Made (Pending)")
    ,SETTLE_PAID_AWAIT_ACK("Settlement Payment Made")
    ,SETTLE_PAID_ACK_PENDING("Settlement Payment Agreed (Pending)")
    ,SETTLE_PAID_ACK("Settlement Payment Agreed")

    ,SETTLE_COMP_AWAIT_ACK_PENDING("Settlement Completed (Pending)")
    ,SETTLE_COMP_AWAIT_ACK("Settlement Completed")
    ,SETTLE_COMPLETE_ACK_PENDING("Settlement Completed Agreed (Pending)")
    ,SETTLE_COMPLETE_ACK("Settlement Completed Agreed");
    
    private final String display;
    
    States(String display) {
        this.display = display;
    }
    
    public String getDisplay() {
        return this.display;
    }
}

