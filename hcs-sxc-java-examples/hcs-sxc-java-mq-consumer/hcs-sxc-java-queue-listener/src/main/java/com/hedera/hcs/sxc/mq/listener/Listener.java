package com.hedera.hcs.sxc.mq.listener;

import com.google.protobuf.InvalidProtocolBufferException;
import com.hedera.hcs.sxc.HCSCore;
import com.hedera.hcs.sxc.callback.OnHCSMessageCallback;
import com.hedera.hcs.sxc.commonobjects.HCSResponse;
import com.hedera.hcs.sxc.consensus.OutboundHCSMessage;
import com.hedera.hcs.sxc.interfaces.SxcApplicationMessageInterface;
import com.hedera.hcs.sxc.commonobjects.SxcConsensusMessage;
import com.hedera.hcs.sxc.interfaces.SxcPersistence;
import com.hedera.hcs.sxc.mq.listener.config.AppData;
import com.hedera.hcs.sxc.mq.listener.config.Config;
import com.hedera.hcs.sxc.mq.listener.config.Queue;
import com.hedera.hcs.sxc.proto.ApplicationMessage;
import com.hedera.hcs.sxc.signing.Signing;
import com.hedera.hcs.sxc.utils.StringUtils;
import com.rabbitmq.client.*;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeoutException;

@Log4j2
@Component
public class Listener {
    private String messageThread = ""; // used to group messages by an conversational id.
    private Map<String, List<String>> messageThreads = new HashMap<String, List<String>>();
    private int topicIndex = 0; // refers to the first topic ID in the config.yaml

    @PostConstruct
    public void init() throws Exception {

        String appId = "RabbitMQ-Listener";

      
        
        
        Queue queueConfig = null;
        try {
            queueConfig = new Config().getConfig().getQueue();
        } catch (IOException ex) {
            log.error(ex);
        }
        
        final Queue queueConfigFinal = queueConfig;
        
        // create a callback object to receive the message
        OnHCSMessageCallback onHCSMessageCallback = new OnHCSMessageCallback(AppData.getHCSCore());
        onHCSMessageCallback.addObserver((SxcConsensusMessage sxcConsensusMesssage, HCSResponse hcsResponse) -> {
            // handle notification in mirrorNotification
            System.out.println("got hcs response - feeding back to out queue");
            //printVerboseDetails(hcsCore, hcsResponse);
            
             ConnectionFactory factory = new ConnectionFactory();
            factory.setHost(queueConfigFinal.getHost());
            factory.setUsername(queueConfigFinal.getUser());
            factory.setPassword(queueConfigFinal.getPassword());
            //factory.setPort(queueConfigFinal.getPort());

            try (Connection connection = factory.newConnection();
                   Channel channel = connection.createChannel()) {

                   channel.exchangeDeclare(queueConfigFinal.getExchangeName(), "topic");

                   //String routingKey = getRouting(argv);
                   //String message = getMessage(argv);

                   channel.basicPublish(queueConfigFinal.getExchangeName(), queueConfigFinal.getProducerTag() , null, getSimpleDetails(AppData.getHCSCore(), hcsResponse).getBytes());
                   System.out.println(" [x] Sent '" + queueConfigFinal.getProducerTag() + "':'" + getSimpleDetails(AppData.getHCSCore(), hcsResponse) + "'");
            } catch (IOException ex) {
                log.error(ex);
            } catch (TimeoutException ex) {
                 log.error(ex);
            } catch (Exception ex) {
                 log.error(ex);
            }
        });


        System.out.println("Loaded APP_ID:" + AppData.getHCSCore().getApplicationId());


        Runnable runnable;
        runnable = () -> {
            ConnectionFactory factory = new ConnectionFactory();
            factory.setHost("localhost");
            factory.setUsername("guest");
            factory.setPassword("guest");
            factory.setPort(5672);

            try (
                    Connection connection = factory.newConnection();
                    Channel channel = connection.createChannel()) {
                //channel.queueDeclare(queueConfig.getName(), false, false, false, null);//(queueConfig.getName(), false, false, false, null);
                channel.exchangeDeclare("testExchange", "topic");
                String consumerTag ="myConsumerTag";
                String queueName = channel.queueDeclare().getQueue();
                channel.queueBind(queueName, "testExchange", consumerTag);
                System.out.println("Connected");
                boolean autoAck = false;
                
                
                DeliverCallback deliverCallback = (consumerTagPrime, delivery) -> {
                    String message = new String(delivery.getBody(), "UTF-8");
                    System.out.println(" [x] Received '" +
                            delivery.getEnvelope().getRoutingKey() + "':'" + message + "'" + "on "+consumerTagPrime);
                    try {
                            OutboundHCSMessage outboundHCSMessage
                                    = new OutboundHCSMessage(AppData.getHCSCore());
                            outboundHCSMessage.sendMessage(0, message.getBytes());
                        } catch (Exception ex) {
                            log.error(ex);
                        }
                };
                channel.basicConsume(queueName, true, deliverCallback, consumerTagPrime -> { 
                    System.out.println("woot");
                });

                Object lock = new Object();
                synchronized (lock) {
                    lock.wait();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        };
        Thread thread = new Thread(runnable);
        thread.start();
    }
    
    private static String getSimpleDetails(HCSCore hcsCore, HCSResponse hcsResponse) {
        String ret=null;
        try {
            SxcApplicationMessageInterface applicationMessageEntity
                    = hcsCore
                            .getPersistence()
                            .getApplicationMessageEntity(
                                    SxcPersistence.extractApplicationMessageStringId(
                                            hcsResponse.getApplicationMessageId()
                                    )
                            );
            ret =  "S:"+applicationMessageEntity.getLastChronoPartSequenceNum()+
                    "T:"+applicationMessageEntity.getLastChronoPartConsensusTimestamp()+
                    "M:"+
                    ApplicationMessage.parseFrom(applicationMessageEntity.getApplicationMessage())
                            .getBusinessProcessMessage().toString("UTF-8");
            ;
            
            
            
        } catch (UnsupportedEncodingException ex) {
            log.error(ex);
        } catch (InvalidProtocolBufferException ex) {
            log.error(ex);
        }
        return ret;
    }
    

    private static void printVerboseDetails(HCSCore hcsCore, HCSResponse hcsResponse) {
        try {
            SxcApplicationMessageInterface applicationMessageEntity
                    = hcsCore
                            .getPersistence()
                            .getApplicationMessageEntity(
                                    SxcPersistence.extractApplicationMessageStringId(
                                            hcsResponse.getApplicationMessageId()
                                    )
                            );
            System.out.println("Details stored as applicationMessageEntity : ");
            System.out.printf("    applicationMessageId: %s \n", applicationMessageEntity.getApplicationMessageId());
            System.out.printf("    last chrono chunk consensus sequenceNum: %s \n", applicationMessageEntity.getLastChronoPartSequenceNum());
            System.out.printf("    last chrono chunk consensus running hash: %s \n", applicationMessageEntity.getLastChronoPartRunningHashHEX());
            System.out.printf("    last chrono chunk consensus timestamp: %s \n", applicationMessageEntity.getLastChronoPartConsensusTimestamp());
            
            System.out.println("    ApplicationMessage: ");
            ApplicationMessage appMessage = ApplicationMessage.parseFrom(applicationMessageEntity.getApplicationMessage());
            System.out.printf("        Id: %s \n", SxcPersistence.extractApplicationMessageStringId(appMessage.getApplicationMessageId()));
            System.out.printf("        Hash of unencrypted message: %s \n",
                    StringUtils.byteArrayToHexString(
                            appMessage.getUnencryptedBusinessProcessMessageHash().toByteArray()
                    )
            );
            System.out.printf("        Signature on hash above: %s \n",
                    StringUtils.byteArrayToHexString(
                            appMessage.getBusinessProcessSignatureOnHash().toByteArray()
                    )
            );

            byte[] bpm = appMessage.getBusinessProcessMessage().toByteArray();
            System.out.printf("        BPM: %s \n",
                    new String(bpm)
            );

            System.out.printf("        Encryption random: %s \n",
                    StringUtils.byteArrayToHexString(
                            appMessage.getEncryptionRandom().toByteArray()
                    )
            );

            System.out.printf("        Is this an echo?: %s \n",
                    Signing.verify(
                            appMessage.getUnencryptedBusinessProcessMessageHash().toByteArray(),
                            appMessage.getBusinessProcessSignatureOnHash().toByteArray(),
                            hcsCore.getMessageSigningKey().publicKey
                    )
            );

        } catch (InvalidProtocolBufferException ex) {
            log.error(ex.getStackTrace());
        }

    }
}
