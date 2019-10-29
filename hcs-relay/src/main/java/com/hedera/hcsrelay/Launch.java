package com.hedera.hcsrelay;

import com.hedera.hcsrelay.config.Config;
import com.hedera.hcsrelay.subscribe.MirrorTopicsSubscriber;

public final class Launch {

    public static void main(String[] args) throws Exception {

        Config config = new Config();
        MirrorTopicsSubscriber topicsSubscriber = new MirrorTopicsSubscriber();
        
        System.out.println("Relay started");

    }

    

}
