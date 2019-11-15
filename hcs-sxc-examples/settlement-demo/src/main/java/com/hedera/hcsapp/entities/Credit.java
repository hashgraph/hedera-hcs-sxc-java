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
    private String transactionId;
    private long threadId;
    private String payerPublicKey;
    private String recipientPublicKey;
    private String serviceRef;
    private long amount;
    private String currency;
    private String memo;
    private String status;
}
