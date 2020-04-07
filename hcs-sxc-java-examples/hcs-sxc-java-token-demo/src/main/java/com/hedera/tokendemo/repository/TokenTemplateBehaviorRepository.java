package com.hedera.tokendemo.repository;

import com.hedera.tokendemo.domain.TokenTemplateBehavior;
import com.hedera.tokendemo.domain.TokenTemplateBehaviorId;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface TokenTemplateBehaviorRepository extends CrudRepository<TokenTemplateBehavior, TokenTemplateBehaviorId> {
    @Query("from TokenTemplateBehavior t where t.tokenTemplateId = ?1")
    List<TokenTemplateBehavior> getBehaviors(long tokenTemplateId);

    @Query("select count(b)> 0 from TokenTemplateBehavior tb INNER JOIN Behavior b on b.id = tb.behaviorId where tb.tokenTemplateId = ?1 and b.name = ?2")
    boolean tokenTemplateHasBehavior(long tokenTemplateId, String behavior);
}
