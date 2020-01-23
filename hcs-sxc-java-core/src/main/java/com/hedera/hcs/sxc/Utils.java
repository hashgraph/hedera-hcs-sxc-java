package com.hedera.hcs.sxc;

public final class Utils {
    public static String getShortKey(String key) {
        return key.substring(0,5) + "..." + key.substring(key.length()-10);
    }
}
