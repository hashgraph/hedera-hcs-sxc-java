package com.hedera.hcs.sxc.cloudwatch.config;

import com.hedera.hcs.sxc.HCSCore;

public final class AppData {

    private static HCSCore hcsCore;

    public static HCSCore getHCSCore() throws Exception {
        if (AppData.hcsCore == null) {
            AppData.hcsCore = new HCSCore().builder("MQ",
                    "./config/config.yaml",
                    "./config/.env"
            );
        }
        return AppData.hcsCore;
    }
}