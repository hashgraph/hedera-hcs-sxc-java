package com.hedera.tokendemo.repository;

import com.hedera.tokendemo.domain.TokenTemplate;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface TokenTemplateRepository extends CrudRepository<TokenTemplate, Long> {
    @Query("from TokenTemplate t where upper(t.name) = upper(?1)")
    Optional<TokenTemplate> findByName(String tokenTypeName);
}
