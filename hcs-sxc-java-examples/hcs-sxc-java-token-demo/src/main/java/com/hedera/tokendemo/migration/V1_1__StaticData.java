package com.hedera.tokendemo.migration;


import com.hedera.tokendemo.domain.Behavior;
import com.hedera.tokendemo.domain.TokenTemplate;
import com.hedera.tokendemo.domain.TokenTemplateBehavior;
import com.hedera.tokendemo.domain.User;
import com.hedera.tokendemo.repository.BehaviorRepository;
import com.hedera.tokendemo.repository.TokenTemplateBehaviorRepository;
import com.hedera.tokendemo.repository.TokenTemplateRepository;
import com.hedera.tokendemo.repository.UserRepository;
import com.hedera.tokendemo.service.UserService;
import org.flywaydb.core.api.migration.BaseJavaMigration;
import org.flywaydb.core.api.migration.Context;
import org.springframework.context.annotation.Lazy;

import javax.inject.Named;

@Named
public class V1_1__StaticData extends BaseJavaMigration {
    private final UserService userService;
    private final BehaviorRepository behaviorRepository;
    private final TokenTemplateRepository tokenTemplateRepository;
    private final TokenTemplateBehaviorRepository tokenTemplateBehaviorRepository;
    // There's a circular dependency of Flyway -> this -> Repositories/JdbcOperations -> Flyway, so use @Lazy to
    // break it.
    // Correct way is to not use repositories and construct manually: new JdbcTemplate(context.getConnection())
    public V1_1__StaticData(@Lazy UserService userService,
            @Lazy BehaviorRepository behaviorRepository,
            @Lazy TokenTemplateRepository tokenTemplateRepository,
            @Lazy TokenTemplateBehaviorRepository tokenTemplateBehaviorRepository) {
        this.userService = userService;
        this.behaviorRepository = behaviorRepository;
        this.tokenTemplateRepository = tokenTemplateRepository;
        this.tokenTemplateBehaviorRepository = tokenTemplateBehaviorRepository;
    }

    @Override
    public void migrate(Context context) throws Exception {
        // create users
        userService.create("Controller","controller pub key", "CBDC");
        userService.create("Junior Controller","jc pub key", "CBDC");
        userService.create("Alice","alice pub key", "GrandCredit");
        userService.create("Bob","bob pub key", "GrandCredit");
        userService.create("Carlos","bob pub key", "Trustcorp");
        userService.create("Dave","dave pub key", "Trustcorp");
        userService.create("GrandCredit Manager","gc pub key", "GrandCredit");
        userService.create("Trustcorp Manager","tc pub key", "Trustcorp");

//        addBehavior("attestable");
        long burnable = addBehavior("burnable");
//      addBehavior("compliant");
//      addBehavior("delegable");
        long divisible = addBehavior("divisible");
//      addBehavior("encumberable");
//      addBehavior("fabricate");
//      addBehavior("indivisible");
//      addBehavior("issuable");
//      addBehavior("logable");
        long mintable = addBehavior("mintable");
//      addBehavior("non-transferable");
//      addBehavior("overdraftable");
//      addBehavior("pausable");
//      addBehavior("redeemable");
//      addBehavior("roles");
//      addBehavior("singleton");
        long transferable = addBehavior("transferable");
//      addBehavior("unique-transferable");
        
        TokenTemplate tokenTemplate = new TokenTemplate();
        tokenTemplate.setName("Demo Token Template");
        tokenTemplate = tokenTemplateRepository.save(tokenTemplate);
        long tokenTemplateId = tokenTemplate.getId();
        // divisible
        TokenTemplateBehavior tokenTemplateBehavior = new TokenTemplateBehavior();
        tokenTemplateBehavior.setTokenTemplateId(tokenTemplateId);
        tokenTemplateBehavior.setBehaviorId(divisible);
        this.tokenTemplateBehaviorRepository.save(tokenTemplateBehavior);
        // mintable
        tokenTemplateBehavior = new TokenTemplateBehavior();
        tokenTemplateBehavior.setTokenTemplateId(tokenTemplateId);
        tokenTemplateBehavior.setBehaviorId(mintable);
        this.tokenTemplateBehaviorRepository.save(tokenTemplateBehavior);
        // transferable
        tokenTemplateBehavior = new TokenTemplateBehavior();
        tokenTemplateBehavior.setTokenTemplateId(tokenTemplateId);
        tokenTemplateBehavior.setBehaviorId(transferable);
        this.tokenTemplateBehaviorRepository.save(tokenTemplateBehavior);
        // burnable
        tokenTemplateBehavior = new TokenTemplateBehavior();
        tokenTemplateBehavior.setTokenTemplateId(tokenTemplateId);
        tokenTemplateBehavior.setBehaviorId(burnable);
        this.tokenTemplateBehaviorRepository.save(tokenTemplateBehavior);
    }
    
    private long addBehavior(String behaviorName) {
        Behavior behavior = new Behavior();
        behavior.setName(behaviorName);
        behavior = this.behaviorRepository.save(behavior);
        return behavior.getId();
    }
}
