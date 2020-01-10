package com.hedera.hcsapp;

public enum States {
    CREDIT_PROPOSED_PENDING("Proposed (Pending)", "N/A")
    ,CREDIT_PROPOSED("Proposed", "N/A")
    ,CREDIT_AGREED_PENDING("Agreed (Pending)", "N/A")
    ,CREDIT_AGREED("Agreed", "N/A")
    
    ,SETTLEMENT_PROPOSED_PENDING("Settlement Proposed (Pending)", "Proposed (Pending)")
    ,SETTLEMENT_PROPOSED("Settlement Proposed", "Proposed")
    ,SETTLEMENT_AGREED_PENDING("Settling", "Agreed (Pending)")
    ,SETTLEMENT_AGREED("Settling", "Agreed")

    ,SETTLE_INIT_AWAIT_ACK_PENDING("Settling", "Payment Channel Proposed (Pending)")
    ,SETTLE_INIT_AWAIT_ACK("Settling", "Payment Channel Proposed")
    ,SETTLE_INIT_ACK_PENDING("Settling", "Payment Channel Agreed (Pending)")
    ,SETTLE_INIT_ACK("Settling", "Payment Channel Agreed")

    ,PAYMENT_INIT_AWAIT_ACK_PENDING("Settling", "Payment Proposed (Pending)")
    ,PAYMENT_INIT_AWAIT_ACK("Settling", "Payment Proposed")
    ,PAYMENT_INIT_ACK_PENDING("Settling", "Payment Agreed (Pending)")
    ,PAYMENT_INIT_ACK("Settling", "Payment Agreed")

    ,PAYMENT_SENT_AWAIT_ACK_PENDING("Settling", "Payment Made (Pending)")
    ,PAYMENT_SENT_AWAIT_ACK("Settling", "Payment Made")
    ,PAYMENT_SENT_ACK_PENDING("Settling", "Payment Agreed (Pending)")
    ,PAYMENT_SENT_ACK("Settling", "Payment Agreed")

    ,SETTLE_PAID_AWAIT_ACK_PENDING("Settling", "Payment Acknowledged (Pending)")
    ,SETTLE_PAID_AWAIT_ACK("Settling", "Payment Acknowledged")
    ,SETTLE_PAID_ACK_PENDING("Settling", "Receipt Requested (Pending)")
    ,SETTLE_PAID_ACK("Settling", "Receipt Requested")

    ,SETTLE_COMP_AWAIT_ACK_PENDING("Settling", "Receipt Confirmed (Pending)")
    ,SETTLE_COMP_AWAIT_ACK("Settling", "Receipt Confirmed")
    ,SETTLE_COMPLETE_ACK_PENDING("Settling", "Complete (Pending)")
    ,SETTLE_COMPLETE_ACK("Settled", "Complete");
    
    private final String displayForCredit;
    private final String displayForSettlement;
    
    States(String displayForCredit, String displayForSettlement) {
        this.displayForCredit = displayForCredit;
        this.displayForSettlement = displayForSettlement;
    }
    
    public String getDisplayForCredit() {
        return this.displayForCredit;
    }
    public String getDisplayForSettlement() {
        return this.displayForSettlement;
    }
}

