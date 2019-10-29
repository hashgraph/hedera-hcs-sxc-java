package com.hedera.hcsrelay.subscribe;

import com.hedera.mirror.api.proto.java.MirrorGetTopicMessages.MirrorGetTopicMessagesResponse;

public final class MirrorMessageHandler {
    public static void onMirrorMessage(MirrorGetTopicMessagesResponse consumer) {
        System.out.println("Got message from mirror node");
        System.out.println("  Consensus TimeStamp: " + consumer.getConsensusTimestamp());
        System.out.println("  Running Hash: " + consumer.getRunningHash());
        System.out.println("  Sequence: " + consumer.getSequenceNumber());
        System.out.println("  Message: "+ consumer.getMessage().toString());
    }
}
