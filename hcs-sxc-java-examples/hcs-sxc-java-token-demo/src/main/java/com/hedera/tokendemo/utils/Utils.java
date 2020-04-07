package com.hedera.tokendemo.utils;

public final class Utils {
    public static String capitalize(final String line) {
        return Character.toUpperCase(line.charAt(0)) + line.substring(1).toLowerCase();
     }
}
