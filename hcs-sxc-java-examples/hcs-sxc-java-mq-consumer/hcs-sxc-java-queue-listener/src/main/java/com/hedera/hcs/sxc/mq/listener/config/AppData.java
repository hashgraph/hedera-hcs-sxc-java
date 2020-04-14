package com.hedera.hcs.sxc.mq.listener.config;

import com.hedera.hcs.sxc.HCSCore;
import lombok.extern.log4j.Log4j2;

@Log4j2
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