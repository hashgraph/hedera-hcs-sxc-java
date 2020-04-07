package com.hedera.tokendemo.repository;

import com.hedera.tokendemo.domain.UserAccount;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface UserAccountRepository extends CrudRepository<UserAccount, Long> {
    @Query("from UserAccount a where a.userId = ?1")
    Optional<UserAccount> findByUserId(long userId);
}
