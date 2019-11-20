package com.hedera.hcsapp.entities;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.Data;

@Data
@Entity
@Table(name = "credits")
public final class Credit {

    @Id
    private String threadId;
    private String transactionId;
    private String payerName;
    private String recipientName;
    private String reference;
    private long amount;
    private String currency;
    private String additionalNotes;
    private String status;
    private String createdDate;
    private String createdTime;
}
