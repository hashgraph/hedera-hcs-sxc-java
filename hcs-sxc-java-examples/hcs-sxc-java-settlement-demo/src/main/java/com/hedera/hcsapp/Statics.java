package com.hedera.hcsapp;


public final class Statics {
    private static AppData appData;

    public static AppData getAppData() throws Exception {
        if (appData == null) {
            appData = new AppData();
        }
        return appData;
    }
}
