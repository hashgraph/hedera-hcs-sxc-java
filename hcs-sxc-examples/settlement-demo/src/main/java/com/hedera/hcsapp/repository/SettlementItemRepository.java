package com.hedera.hcsapp.repository;

import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import com.hedera.hcsapp.entities.SettlementItem;
import com.hedera.hcsapp.entities.SettlementItemId;

public interface SettlementItemRepository extends CrudRepository<SettlementItem, SettlementItemId> {
    
    @Query("SELECT si FROM SettlementItem si WHERE si.id.threadId = :threadId")
    List<SettlementItem> findAllSettlementItems(@Param("threadId") String threadId);
}

