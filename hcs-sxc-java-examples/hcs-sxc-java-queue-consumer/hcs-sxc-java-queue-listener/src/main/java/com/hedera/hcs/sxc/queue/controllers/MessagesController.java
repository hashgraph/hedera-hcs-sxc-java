package com.hedera.hcs.sxc.queue.controllers;

import com.hedera.hcs.sxc.interfaces.SxcApplicationMessageInterface;
import com.hedera.hcs.sxc.interfaces.SxcPersistence;
import com.hedera.hcs.sxc.queue.config.AppData;
import com.hedera.hcs.sxc.proto.ApplicationMessage;
import lombok.Data;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;
import java.util.Map.Entry;
import java.util.stream.Collectors;

@RestController
public class MessagesController {

    @Data
    class HCSMessage {
        String consensusTimeStamp;
        String message;
        String runningHash;
        long sequence;
    }

    public MessagesController() throws Exception {
    }

    @GetMapping(value = "/hcs", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<HCSMessage> hcsMessages() throws Exception {
        List<HCSMessage> hcsMessages = new ArrayList<>();
        // get messages from persistence
        Map<String, ApplicationMessage> applicationMessages = AppData.getHCSCore().getPersistence().getApplicationMessages();
        // sort in reverse order of consensus (newest first)
        Map<String, ApplicationMessage> sortedApplicationMessages = applicationMessages.entrySet().stream()
                .sorted(Map.Entry.comparingByKey(Comparator.reverseOrder()))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue,
                        (oldValue, newValue) -> oldValue, LinkedHashMap::new));
        // convert to list of objects for return
        for (Entry<String, ApplicationMessage> entry : sortedApplicationMessages.entrySet()) {
            HCSMessage hcsMessage = new HCSMessage();
            
            
            
            SxcApplicationMessageInterface applicationMessageEntity
            = AppData.getHCSCore()
                    .getPersistence()
                    .getApplicationMessageEntity(
                            SxcPersistence.extractApplicationMessageStringId(
                                    entry.getValue().getApplicationMessageId()
                            )
                    );
            
            if ((applicationMessageEntity != null) && (applicationMessageEntity.getLastChronoPartConsensusTimestamp() != null)) {
                hcsMessage.consensusTimeStamp = applicationMessageEntity.getLastChronoPartConsensusTimestamp().toString();
                ApplicationMessage applicationMessage = ApplicationMessage.parseFrom(applicationMessageEntity.getApplicationMessage());
                hcsMessage.message = applicationMessage.getBusinessProcessMessage().toStringUtf8();
                hcsMessage.runningHash = applicationMessageEntity.getLastChronoPartRunningHashHEX();
                hcsMessage.sequence = applicationMessageEntity.getLastChronoPartSequenceNum();
    
                hcsMessages.add(hcsMessage);
            }
        }

        return hcsMessages;
    }
}
