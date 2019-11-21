package com.hedera.hcsapp.repository;

import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import com.hedera.hcsapp.entities.Settlement;

public interface SettlementRepository extends CrudRepository<Settlement, String> {
    
    @Query("SELECT s FROM Settlement s WHERE (s.payerName = :currentUser AND s.recipientName = :searchUser) OR (s.payerName = :searchUser AND s.recipientName = :currentUser)")
    List<Settlement> findAllSettlementsForUsers(@Param("currentUser") String currentUser, @Param("searchUser") String searchUser);
}

