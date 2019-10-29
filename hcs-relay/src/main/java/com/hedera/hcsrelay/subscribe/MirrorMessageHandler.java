package com.hedera.hcsrelay.subscribe;

import com.hedera.hashgraph.sdk.consensus.TopicId;
import com.hedera.hcsrelay.config.Config;
import com.hedera.mirror.api.proto.java.MirrorGetTopicMessages.MirrorGetTopicMessagesResponse;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class MirrorMessageHandler {
   
    
    public static void onMirrorMessage(MirrorGetTopicMessagesResponse messagesResponse, TopicId topicId) {
        try {
            Config config = new Config();
            
            
            System.out.println("Got message from mirror node");
            System.out.println("  Topic number: " + topicId.getTopicNum());
            System.out.println("  Consensus TimeStamp: " + messagesResponse.getConsensusTimestamp());
            System.out.println("  Running Hash: " + messagesResponse.getRunningHash());
            System.out.println("  Sequence: " + messagesResponse.getSequenceNumber());
            System.out.println("  Message: "+ messagesResponse.getMessage().toString());
            
         
            
            if (!config.getConfig().getTopicIds().contains(topicId)) throw new Exception("Received undifined topicId from mirror: " + topicId);
            QueueTopicOperations.addMessage(config, messagesResponse, topicId);
            
        } catch (Exception ex) {
            Logger.getLogger(MirrorMessageHandler.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        
        
    }
}
