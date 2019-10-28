package com.hedera.hcsrelay;

import com.hedera.hcsrelay.subscribe.MirrorTopicsSubscribers;

public final class Launch {

    public static void main(String[] args) throws Exception {
        MirrorTopicsSubscribers topicsSubscriber = new MirrorTopicsSubscribers();
        System.out.println("Relay started");
    }
}
