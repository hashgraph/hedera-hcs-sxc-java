package com.hedera.hcsapp.entities;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.Data;

@Data
@Entity
@Table(name = "settlements")
public final class Settlement {

    @Id
    private String threadId;
    private String applicationMessageId;
    private String payerName;
    private String recipientName;
    private String additionalNotes;
    private long netValue;
    private String currency;
    @Column(length = 40)
    private String status;
    private String createdDate;
    private String createdTime;
    private String paymentChannelName;
    private String payerAccountDetails;
    private String recipientAccountDetails;
    private String paymentReference;
}
