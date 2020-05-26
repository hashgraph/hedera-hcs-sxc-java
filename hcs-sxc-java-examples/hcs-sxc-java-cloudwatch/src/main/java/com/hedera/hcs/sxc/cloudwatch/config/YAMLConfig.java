package com.hedera.hcs.sxc.cloudwatch.config;

public final class YAMLConfig {

    private Cloudwatch cloudwatch = new Cloudwatch();

    public Cloudwatch getCloudwatch() {
        return this.cloudwatch;
    }
    public void setCloudwatch(Cloudwatch cloudwatch) {
        this.cloudwatch = cloudwatch;
    }
}
