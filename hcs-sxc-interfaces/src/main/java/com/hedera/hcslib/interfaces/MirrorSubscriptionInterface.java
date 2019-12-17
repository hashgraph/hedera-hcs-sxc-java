package com.hedera.hcslib.interfaces;

import java.time.Instant;
import java.util.Optional;

public interface MirrorSubscriptionInterface {
    public void init(HCSCallBackFromMirror onHCSMessageCallback, long applicationId, Optional<Instant> lastConsensusTimestamp) throws Exception;    
}
