package com.hedera.tokendemo.repository;

import com.hedera.tokendemo.domain.Token;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import java.util.List;
import java.util.Optional;

public interface TokenRepository extends CrudRepository<Token, Long> {
    @Query("from Token t where upper(t.name) = upper(?1)")
    Optional<Token> findByName(String tokenName);

    @Query("from Token t where upper(t.symbol) = upper(?1)")
    Optional<Token> findBySymbol(String tokenSymbol);

    public List<Token> findAllByOrderByIdDesc();
}
