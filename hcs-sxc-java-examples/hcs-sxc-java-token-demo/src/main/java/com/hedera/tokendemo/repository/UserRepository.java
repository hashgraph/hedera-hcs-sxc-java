package com.hedera.tokendemo.repository;

import com.hedera.tokendemo.domain.User;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends CrudRepository<User, Long> {
    @Query("from User u where upper(u.name) = upper(?1)")
    Optional<User> findByName(String userName);

    @Query("from User u where upper(u.userOf) = upper(?1)")
    List<User> getUsers(String userOf);

    @Query("from User u inner join UserAccount ua on ua.accountId = ?1 and ua.userId = u.id")
    List<User> usersForAccount(long accountId);
}
