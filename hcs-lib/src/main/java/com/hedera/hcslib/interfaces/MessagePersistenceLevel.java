package com.hedera.hcslib.interfaces;

public enum MessagePersistenceLevel {
    NONE  // no message persistence
    ,MESSAGE_ONLY // only persist message
    ,MESSAGE_AND_PARTS // persist message and message parts
    ,FULL // persist all communications with Hedera (HCS and Mirror Node)
}
