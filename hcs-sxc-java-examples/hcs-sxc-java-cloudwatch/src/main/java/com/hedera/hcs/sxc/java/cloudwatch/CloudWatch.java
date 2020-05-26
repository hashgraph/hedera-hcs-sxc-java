package com.hedera.hcs.sxc.java.cloudwatch;

import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.List;
import software.amazon.awssdk.services.cloudwatch.model.CloudWatchException;
import software.amazon.awssdk.services.cloudwatchlogs.CloudWatchLogsClient;
import software.amazon.awssdk.services.cloudwatchlogs.model.GetLogEventsRequest;
import software.amazon.awssdk.services.cloudwatchlogs.model.OutputLogEvent;
import org.apache.commons.codec.binary.Hex;
import org.json.simple.JSONObject;

import com.hedera.hcs.sxc.cloudwatch.config.AppData;
import com.hedera.hcs.sxc.cloudwatch.config.Cloudwatch;
import com.hedera.hcs.sxc.cloudwatch.config.Config;
import com.hedera.hcs.sxc.consensus.OutboundHCSMessage;

/**
 * Gets log events from CloudWatch
 */
public class CloudWatch {

    public static void main(String[] args) throws Exception {

        Cloudwatch cloudwatchConfig = new Config().getConfig().getCloudwatch();
        String logStreamName = cloudwatchConfig.getLogStreamName();
        String logGroupName = cloudwatchConfig.getLogGroupName();
        int batchSize = cloudwatchConfig.getBatchSize();
        
        // Create a CloudWatchLogClient
        CloudWatchLogsClient cloudWatchLogsClient = CloudWatchLogsClient.builder()
                .build();
        long startTime = 1588794992590L; //Instant.now().toEpochMilli();
        System.out.println("Time is: " + startTime);
        while (true) {
            startTime = getCWLogEvents(batchSize, startTime, cloudWatchLogsClient, logGroupName, logStreamName);
            startTime += 1;
            Thread.sleep(10000);
        }
    }

    public static void batchToHCS(String batchData, long firstLog, long lastLog, int batchSize) throws Exception {
        MessageDigest digest = MessageDigest.getInstance("SHA-384");
        byte[] encodedhash = digest.digest(
                batchData.getBytes(StandardCharsets.UTF_8));
        String stringHash = Hex.encodeHexString(encodedhash);
        
        JSONObject obj = new JSONObject();
        obj.put("startTimestamp", firstLog);
        obj.put("endTimestamp", lastLog); 
        obj.put("logSha384", stringHash);
        
        StringWriter out = new StringWriter();
        obj.writeJSONString(out);
        
        String jsonText = out.toString();

        System.out.println("");
        System.out.println("****************************************************************");
        System.out.println("Sending batch hash to HCS - batch size " + batchSize);
        System.out.println(batchData);
        System.out.println("****************************************************************");
        System.out.println("logSha384:" + stringHash);
        System.out.println("startTimestamp:" + firstLog);
        System.out.println("endTimestamp:" + lastLog);
        System.out.println("****************************************************************");
        System.out.println("");
        
        try {
            OutboundHCSMessage outboundHCSMessage = new OutboundHCSMessage(AppData.getHCSCore());
            outboundHCSMessage.sendMessage(0, obj.toJSONString().getBytes());
        } catch (Exception ex) {
            System.out.println(ex);
        }
        
    }
    
    public static long getCWLogEvents(int batchSize, long startTime, CloudWatchLogsClient cloudWatchLogsClient, String logGroupName, String logStreamName) throws Exception {
        try {
            // Designate the logGroupName and logStream you want to get logs from
            // Assume only one stream name exists, however, this isn't always the case
            GetLogEventsRequest getLogEventsRequest = GetLogEventsRequest.builder()
                .logGroupName(logGroupName)
                .logStreamName(logStreamName)
                .startFromHead(true)
                .startTime(startTime)
                .build();

            String allMessages = "";
            List<OutputLogEvent> logEvents = cloudWatchLogsClient.getLogEvents(getLogEventsRequest).events();
            if (logEvents.size() > 0) {
                int batchItem = 1;
                long firstLog = 0;
                for (OutputLogEvent logEvent : logEvents) {
                    String message = logEvent.message();
                    System.out.println(message);
                    if (batchItem == 1) {
                        firstLog = logEvent.timestamp();
                        allMessages = message;
                    } else {
                        startTime = logEvent.timestamp();
                        allMessages = allMessages.concat(message);
                    }
                    batchItem += 1;

                    if (batchItem == batchSize + 1) {
                        // batch complete, package and send to HCS
                        batchToHCS(allMessages, firstLog, startTime, batchSize); // start time is the timestamp of the last event processed
                        // reset and continue
                        batchItem = 1;
                        allMessages = "";
                    }
                } 
                if ( ! allMessages.isEmpty()) {
                    // finalise the batch
                    batchToHCS(allMessages, firstLog, startTime, batchItem); // start time is the timestamp of the last event processed
                }
            }
            System.out.println("Successfully got " + logEvents.size() + " CloudWatch log events!");
            
        } catch (CloudWatchException e) {
            System.err.println(e.awsErrorDetails().errorMessage());
            System.exit(1);
        }

        return startTime;
    }
}