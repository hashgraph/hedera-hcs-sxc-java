package com.hedera.hcsrelay.subscribe;

import com.hedera.hashgraph.sdk.consensus.TopicId;
import com.hedera.hcsrelay.config.Config;
import com.hedera.mirror.api.proto.java.MirrorGetTopicMessages.MirrorGetTopicMessagesResponse;

import lombok.extern.log4j.Log4j2;

@Log4j2
public final class MirrorMessageHandler {
   
    
    public static void onMirrorMessage(MirrorGetTopicMessagesResponse messagesResponse, TopicId topicId) {
        try {
            Config config = new Config();
            
            log.info("Got message from mirror node");
            log.info("  Topic number: " + topicId.getTopicNum());
            log.info("  Consensus TimeStamp: " + messagesResponse.getConsensusTimestamp());
            log.info("  Running Hash: " + messagesResponse.getRunningHash());
            log.info("  Sequence: " + messagesResponse.getSequenceNumber());
            //log.info("  Sequence: " + messagesResponse.;
            
            log.info("  Message: "+ messagesResponse.getMessage().toStringUtf8());
            
            QueueTopicOperations.addMessage(config, messagesResponse, topicId);
            log.info("Message added to queue");
        } catch (Exception ex) {
            log.error(ex);
        }
        
        
        
    }
}
