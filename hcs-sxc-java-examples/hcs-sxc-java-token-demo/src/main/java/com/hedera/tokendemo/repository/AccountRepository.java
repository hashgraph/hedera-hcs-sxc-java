package com.hedera.tokendemo.repository;

import com.hedera.tokendemo.domain.Account;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface AccountRepository extends CrudRepository<Account, Long> {
    @Query("from Account a inner join UserAccount ua on a.id = ua.accountId where a.tokenId = ?1 and ua.userId = ?2")
    List<Account> findByTokenAndUserId(long tokenId, long userId);

    @Query("from Account a where a.tokenId = ?1")
    List<Account> findByTokenId(long tokenId);
}
