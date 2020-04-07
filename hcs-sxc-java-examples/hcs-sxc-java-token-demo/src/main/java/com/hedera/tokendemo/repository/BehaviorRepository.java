package com.hedera.tokendemo.repository;

import com.hedera.tokendemo.domain.Behavior;
import org.springframework.data.repository.CrudRepository;

public interface BehaviorRepository extends CrudRepository<Behavior, Long> {
//    @Query("from Account a where upper(a.name) = upper(?1)")
//    Optional<Account> findByName(String accountName);
}
