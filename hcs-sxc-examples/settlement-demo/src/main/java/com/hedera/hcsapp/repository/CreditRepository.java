package com.hedera.hcsapp.repository;

import org.springframework.data.repository.CrudRepository;

import com.hedera.hcsapp.entities.Credit;

public interface CreditRepository extends CrudRepository<Credit, String> {
}

