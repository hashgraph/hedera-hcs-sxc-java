package com.hedera.hcsapp.repository;

import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import com.hedera.hcsapp.entities.Credit;

public interface CreditRepository extends CrudRepository<Credit, String> {
    
    @Query("SELECT c FROM Credit c WHERE (c.payerName = :currentUser AND c.recipientName = :searchUser) OR (c.payerName = :searchUser AND c.recipientName = :currentUser)")
    List<Credit> findAllCreditsForUsers(@Param("currentUser") String currentUser, @Param("searchUser") String searchUser);
}

