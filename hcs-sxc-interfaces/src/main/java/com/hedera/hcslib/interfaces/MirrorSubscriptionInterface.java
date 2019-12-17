package com.hedera.hcslib.interfaces;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.time.Instant;
import java.util.Optional;

public interface MirrorSubscriptionInterface {
    public void init (HCSCallBackFromMirror onHCSMessageCallback, long applicationId, Optional<Instant> subscribeFromInstant) throws FileNotFoundException, IOException;
    
}
