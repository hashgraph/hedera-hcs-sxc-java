package com.hedera.plugin.persistence.entities;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.Data;

@Entity
@Data
@Table(name = "ApplicationMessages")
public class HCSApplicationMessage {
    @Id
    private String applicationMessageId;
    private byte[] applicationMessage;
}
