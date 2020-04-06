package com.hedera.tokendemo.repository;

import com.hedera.tokendemo.domain.Operation;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface OperationRepository extends CrudRepository<Operation, Long> {
    @Query("from Operation o where upper(o.operator) = upper(?1) or upper(o.recipient) = upper(?1) order by id desc")
    List<Operation> findByUserName(String userName);
}
