package com.hedera.hcs.sxc.plugin.persistence.entities;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.Data;

@Entity
@Data
@Table(name = "Transactions")
public class HCSTransaction {
    @Id
    private String transactionId;
    private byte[] bodyBytes;
}
