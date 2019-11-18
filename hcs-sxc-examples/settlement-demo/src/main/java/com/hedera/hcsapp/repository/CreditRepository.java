package com.hedera.hcsapp.repository;

import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import com.hedera.hcsapp.entities.Credit;

public interface CreditRepository extends CrudRepository<Credit, String> {
    
    @Query("SELECT c FROM Credit c WHERE (c.payerPublicKey = :currentKey AND c.recipientPublicKey = :searchKey) OR (c.payerPublicKey = :searchKey AND c.recipientPublicKey = :currentKey)")
    List<Credit> findAllCreditsForKeys(@Param("currentKey") String currentKey, @Param("searchKey") String searchKey);
}

