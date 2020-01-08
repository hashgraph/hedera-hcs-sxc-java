package com.hedera.hcslib;

public final class Utils {
    public static String getShortKey(String key) {
        return key.substring(0,5) + "..." + key.substring(key.length()-10);
    }
}
