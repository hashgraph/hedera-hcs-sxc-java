package com.hedera.hcs.sxc.cloudwatch.config;

public final class Cloudwatch {
    private String logStreamName = "";
    private String logGroupName =  "";
    private int batchSize = 10;
    
    public String getLogStreamName() {
        return this.logStreamName;
    }

    public void setLogStreamName(String logStreamName) {
        this.logStreamName = logStreamName;
    }

    public String getLogGroupName() {
        return this.logGroupName;
    }

    public void setLogGroupName(String logGroupName) {
        this.logGroupName = logGroupName;
    }
    
    public int getBatchSize() {
        return this.batchSize;
    }
    public void setBatchSize(int batchSize) {
        this.batchSize = batchSize;
    }
}
