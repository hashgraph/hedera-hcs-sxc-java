package com.hedera.hcsapp.entities;

import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.Table;

import lombok.Data;

@Data
@Entity
@Table(name = "settlement_items")
public final class SettlementItem {

    @EmbeddedId
    private SettlementItemId id;
}
