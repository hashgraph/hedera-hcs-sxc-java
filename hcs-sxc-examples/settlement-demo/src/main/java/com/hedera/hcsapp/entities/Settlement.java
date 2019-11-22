package com.hedera.hcsapp.entities;

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
    private String transactionId;
    private String payerName;
    private String recipientName;
    private String additionalNotes;
    private long netValue;
    private String currency;
    private String status;
    private String createdDate;
    private String createdTime;
}
