package com.hedera.hcsrelay;

import com.hedera.hcsrelay.config.Config;
import com.hedera.hcsrelay.subscribe.MirrorTopicsSubscribers;

public final class Launch {

    public static void main(String[] args) throws Exception {
        Config config = new Config();
        
        String mirrorAddress = config.getConfig().getMirrorAddress();
        String[] mirrorDetails = mirrorAddress.split(":");
        if (mirrorDetails.length != 2) {
            throw new Exception("hcs-relay: mirrorAddress format is incorrect, should be address:port");
        }
        
        MirrorTopicsSubscribers topicsSubscriber = new MirrorTopicsSubscribers(mirrorDetails[0], Integer.parseInt(mirrorDetails[1]), config);
    }
}
