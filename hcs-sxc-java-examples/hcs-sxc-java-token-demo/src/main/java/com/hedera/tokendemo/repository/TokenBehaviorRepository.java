package com.hedera.tokendemo.repository;

import com.hedera.tokendemo.domain.Behavior;
import com.hedera.tokendemo.domain.TokenBehavior;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface TokenBehaviorRepository extends CrudRepository<TokenBehavior, Long> {
    @Query("select count(b)> 0 from TokenBehavior tb INNER JOIN Behavior b on b.id = tb.behaviorId where tb.tokenId = ?1 and b.name = ?2")
    boolean tokenHasBehavior(long tokenId, String behavior);

    @Query("select b from TokenBehavior tb inner join Behavior b on b.id = tb.behaviorId where tb.tokenId = ?1")
    List<Behavior> findTokenBehaviors(long tokenId);
}
