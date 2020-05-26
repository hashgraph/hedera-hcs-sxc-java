package com.hedera.hcs.sxc.queue;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import org.bouncycastle.util.encoders.Hex;

import com.google.gson.JsonObject;
import com.google.protobuf.InvalidProtocolBufferException;
import com.hedera.hcs.sxc.HCSCore;
import com.hedera.hcs.sxc.commonobjects.HCSResponse;
import com.hedera.hcs.sxc.commonobjects.SxcConsensusMessage;
import com.hedera.hcs.sxc.interfaces.SxcApplicationMessageInterface;
import com.hedera.hcs.sxc.interfaces.SxcPersistence;
import com.hedera.hcs.sxc.proto.ApplicationMessage;

import lombok.extern.log4j.Log4j2;

@Log4j2
public final class Utils {
    public static String getSimpleDetails(HCSCore hcsCore, HCSResponse hcsResponse) {
        String ret = null;
        try {
            SxcApplicationMessageInterface applicationMessageEntity = hcsCore.getPersistence()
                    .getApplicationMessageEntity(
                            SxcPersistence.extractApplicationMessageStringId(hcsResponse.getApplicationMessageId()));
            ret =   hcsCore.getTopics().get(0).getTopic() + "|"
                    + applicationMessageEntity.getLastChronoPartSequenceNum() + "|"
                    + applicationMessageEntity.getLastChronoPartConsensusTimestamp() + "|"
                    + ApplicationMessage.parseFrom(applicationMessageEntity.getApplicationMessage())
                            .getBusinessProcessMessage().toString("UTF-8");
            ;

        } catch (UnsupportedEncodingException ex) {
            log.error(ex);
        } catch (InvalidProtocolBufferException ex) {
            log.error(ex);
        }
        return ret;
    }
    public static List<HCSMessageRest> restResponse(HCSCore hcsCore) throws InvalidProtocolBufferException {
        List<HCSMessageRest> hcsMessages = new ArrayList<>();
        // get messages from persistence
        Map<String, ApplicationMessage> applicationMessages = hcsCore.getPersistence().getApplicationMessages();
        // sort in reverse order of consensus (newest first)
        Map<String, ApplicationMessage> sortedApplicationMessages = applicationMessages.entrySet().stream()
                .sorted(Map.Entry.comparingByKey(Comparator.reverseOrder()))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue,
                        (oldValue, newValue) -> oldValue, LinkedHashMap::new));
        // convert to list of objects for return
        for (Entry<String, ApplicationMessage> entry : sortedApplicationMessages.entrySet()) {
            HCSMessageRest hcsMessage = new HCSMessageRest();
            
            SxcApplicationMessageInterface applicationMessageEntity
            = hcsCore
                    .getPersistence()
                    .getApplicationMessageEntity(
                            SxcPersistence.extractApplicationMessageStringId(
                                    entry.getValue().getApplicationMessageId()
                            )
                    );
            
            if ((applicationMessageEntity != null) && (applicationMessageEntity.getLastChronoPartConsensusTimestamp() != null)) {
                hcsMessage.setConsensusTimeStamp(applicationMessageEntity.getLastChronoPartConsensusTimestamp().toString());
                ApplicationMessage applicationMessage = ApplicationMessage.parseFrom(applicationMessageEntity.getApplicationMessage());
                hcsMessage.setMessage(applicationMessage.getBusinessProcessMessage().toStringUtf8());
                hcsMessage.setRunningHash(applicationMessageEntity.getLastChronoPartRunningHashHEX());
                hcsMessage.setSequence(applicationMessageEntity.getLastChronoPartSequenceNum());
    
                hcsMessages.add(hcsMessage);
            }
        }
    
        return hcsMessages;
    }
    public static String JSONPublishMessage(SxcConsensusMessage sxcConsensusMesssage, HCSResponse hcsResponse) {
        
        JsonObject response = new JsonObject();
        response.addProperty("topicId", sxcConsensusMesssage.topicId.toString());
        response.addProperty("consensusTimestamp", sxcConsensusMesssage.consensusTimestamp.toString());
        response.addProperty("runningHash", Hex.toHexString(sxcConsensusMesssage.runningHash));
        response.addProperty("sequence", sxcConsensusMesssage.sequenceNumber);
        response.addProperty("message", Hex.toHexString(hcsResponse.getMessage()));
        
        return response.toString();
    }
}
