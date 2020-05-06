package com.hedera.hcsapp;


import com.hedera.hashgraph.sdk.TransactionId;
import com.hedera.hcs.sxc.HCSCore;
import com.hedera.hcs.sxc.callback.OnHCSMessageCallback;
import com.hedera.hcs.sxc.commonobjects.HCSResponse;
import com.hedera.hcs.sxc.commonobjects.SxcConsensusMessage;
import com.hedera.hcs.sxc.consensus.OutboundHCSMessage;
import com.hedera.hcs.sxc.interfaces.SxcApplicationMessageInterface;
import com.hedera.hcs.sxc.interfaces.SxcPersistence;

import com.hedera.hcs.sxc.proto.ApplicationMessage;
import com.hedera.hcs.sxc.proto.ApplicationMessageChunk;
import java.io.PrintStream;

import lombok.Data;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;

@RestController
public class MessagesController {
    
    HCSCore hcsCore;
   

    @Data
    class HCSMessage {
        String consensusTimeStamp;
        String message;
        String runningHash;
        long sequence;
    }

    public MessagesController() throws Exception {
        hcsCore =  new HCSCore().builder("MQ",
                    "./config/config.yaml",
                    "./config/.env"
        );
        PrintStream out = System.out;
        out.println("****************************************");
        out.println("** Welcome to PDF HCS demo");
        out.println("** I am app: " + hcsCore.getApplicationId());
        out.println("** My private signing key is: " + hcsCore.getMessageSigningKey());
        out.println("** My public signing key is: " + hcsCore.getMessageSigningKey().publicKey);
        out.println("****************************************");
        
        
        OnHCSMessageCallback onHCSMessageCallback = new OnHCSMessageCallback(hcsCore);
        onHCSMessageCallback.addObserver((SxcConsensusMessage sxcConsensusMesssage, HCSResponse hcsResponse) -> {
            System.out.println(sxcConsensusMesssage.getMessageString());
        });
        
    }

    @Async
    @PostMapping(path = "/hcs-open-pdf")
    public @ResponseBody ResponseEntity<String> onDocumentOpen(@RequestBody String payload) throws Exception {
        System.out.println(payload);
        OutboundHCSMessage outboundHCSMessage = new OutboundHCSMessage(hcsCore);
        outboundHCSMessage.sendMessage(0, payload.getBytes());
        //TransactionId firstTransactionId = outboundHCSMessage.getFirstTransactionId();
       
        List<? extends SxcApplicationMessageInterface> sxcApplicationMessages = hcsCore.getPersistence().getSXCApplicationMessages();
        while (sxcApplicationMessages.isEmpty() || sxcApplicationMessages.get(0).getLastChronoPartConsensusTimestamp() == null) {
            Thread.sleep(10000);
            sxcApplicationMessages = hcsCore.getPersistence().getSXCApplicationMessages();   
        }
        SxcApplicationMessageInterface appMsg = sxcApplicationMessages.get(0);
        hcsCore.getPersistence().clear();
        
        return new ResponseEntity<>("The payload: "+ payload + " has been received by HCS. \nSeqNO: " + appMsg.getLastChronoPartSequenceNum() + "\nConsensus timestamp: "+ appMsg.getLastChronoPartConsensusTimestamp() , HttpStatus.OK);
    }
    
    
    
    @GetMapping(value = "/hcs", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<HCSMessage> hcsMessages() throws Exception {
        List<HCSMessage> hcsMessages = new ArrayList<>();
        // get messages from persistence
        Map<String, ApplicationMessage> applicationMessages = hcsCore.getPersistence().getApplicationMessages();
        // sort in reverse order of consensus (newest first)
        Map<String, ApplicationMessage> sortedApplicationMessages = applicationMessages.entrySet().stream()
                .sorted(Map.Entry.comparingByKey(Comparator.reverseOrder()))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue,
                        (oldValue, newValue) -> oldValue, LinkedHashMap::new));
        // convert to list of objects for return
        for (Entry<String, ApplicationMessage> entry : sortedApplicationMessages.entrySet()) {
            HCSMessage hcsMessage = new HCSMessage();
            
            
            
            SxcApplicationMessageInterface applicationMessageEntity
            = hcsCore
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
