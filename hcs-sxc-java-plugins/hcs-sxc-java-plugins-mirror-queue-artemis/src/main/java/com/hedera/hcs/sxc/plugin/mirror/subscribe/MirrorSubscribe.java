package com.hedera.hcs.sxc.plugin.mirror.subscribe;

import java.time.Instant;
import java.util.Hashtable;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import javax.jms.ConnectionFactory;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.Session;
import javax.jms.Topic;
import javax.jms.TopicSubscriber;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.apache.activemq.artemis.jms.client.ActiveMQObjectMessage;
import org.apache.activemq.artemis.jms.client.ActiveMQTextMessage;

import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import com.hedera.hashgraph.proto.Timestamp;
import com.hedera.hashgraph.proto.mirror.ConsensusTopicResponse;
import com.hedera.hashgraph.sdk.consensus.ConsensusTopicId;
import com.hedera.hcs.sxc.commonobjects.SxcConsensusMessage;
import com.hedera.hcs.sxc.interfaces.HCSCallBackFromMirror;
import com.hedera.hcs.sxc.interfaces.HCSRelayMessage;
import com.hedera.hcs.sxc.interfaces.MirrorSubscriptionInterface;
import com.hedera.hcs.sxc.plugin.mirror.config.Config;
import com.hedera.hcs.sxc.proto.ApplicationMessageChunk;

import lombok.extern.log4j.Log4j2;

@Log4j2
public class MirrorSubscribe implements MirrorSubscriptionInterface {

    @Override
    public void init(HCSCallBackFromMirror onHCSMessageCallback, long applicationId, Optional<Instant> lastConsensusTimestamp, String mirrorAddress, List<ConsensusTopicId> topicIds) throws Exception {
        Config  config = new Config();
        log.info("hcs-sxc-java-plugins-mirror-queue-artemis init");
        String contextFactory = config.getConfig().getQueue().getInitialContextFactory();
        String tcpConnectionFactory = config.getConfig().getQueue().getTcpConnectionFactory();
        Runnable runnable;
        runnable = () -> { 
            InitialContext initialContext = null;
            javax.jms.Connection connection = null;
            try {
                
                Hashtable<String, Object> props = new Hashtable<>();
                props.put(Context.INITIAL_CONTEXT_FACTORY, contextFactory);
                props.put("topic.topic/hcsTopic", "hcsCatchAllTopics");
                props.put("connectionFactory.TCPConnectionFactory", tcpConnectionFactory);
                InitialContext ctx = new InitialContext(props);
                
                ctx.lookup("TCPConnectionFactory");

                initialContext = ctx;

                Topic topic = (Topic) initialContext.lookup("topic/hcsTopic");

                ConnectionFactory cf = (ConnectionFactory) initialContext.lookup("TCPConnectionFactory");

                boolean retry = true;
                while (retry) {
                    try {
                        connection = cf.createConnection();
                        retry = false;
                        log.info("Connected to message queue");
                    }
                    catch (JMSException ex) {
                        log.error(ex);
                        log.info("Unable to connect to message queue - sleeping 5s");
                        TimeUnit.SECONDS.sleep(5);
                    }
                }

                connection.setClientID("operator-client-" + applicationId);

                connection.start();

                Session session = connection.createSession(false, Session.CLIENT_ACKNOWLEDGE);

                TopicSubscriber subscriber = session.createDurableSubscriber(topic, "subscriber-hcsCatchAllTopics");

                subscriber.setMessageListener(new MessageListener() {
                    @Override
                    public void onMessage(Message messageFromJMS) {
                        try {
                            // notify subscribed observer from App.java
                            if (messageFromJMS instanceof ActiveMQTextMessage) {
                                log.info("Got message from queue - notifying");
                                onHCSMessageCallback.notifyObservers(((ActiveMQTextMessage)messageFromJMS).getText().getBytes(), null);
                                messageFromJMS.acknowledge();
                                log.info("Got message from queue - acknowledged");
                            } else if (messageFromJMS instanceof ActiveMQObjectMessage) {
                                log.info("Got message from queue - persisting");
                                HCSRelayMessage rlm = (HCSRelayMessage)((ActiveMQObjectMessage) messageFromJMS).getObject();
                                
                                ConsensusTopicResponse consensusTopicResponse = ConsensusTopicResponse.newBuilder()
                                        .setConsensusTimestamp(Timestamp.newBuilder().setSeconds(rlm.getConsensusTimestamp().getEpochSecond()).setNanos(rlm.getConsensusTimestamp().getNano()).build())
                                        .setMessage(ByteString.copyFrom(rlm.getMessage()))
                                        .setRunningHash(ByteString.copyFrom(rlm.getRunningHash()))
                                        .setSequenceNumber(rlm.getSequenceNumber())
                                        .build();
                                ConsensusTopicId topicId = new ConsensusTopicId(rlm.getTopicShard(), rlm.getTopicRealm(), rlm.getTopicNum());
                                SxcConsensusMessage consensusMessage = new SxcConsensusMessage(topicId, consensusTopicResponse);

                                onHCSMessageCallback.storeMirrorResponse(consensusMessage);
                                
                                byte[] message = rlm.getMessage();
                                ApplicationMessageChunk messagePart = ApplicationMessageChunk.parseFrom(message);

                                onHCSMessageCallback.partialMessage(messagePart);
                                messageFromJMS.acknowledge();

                                log.info("Got message from queue - acknowledged");
                            }
                            
                        } catch (JMSException ex) {
                            log.error(ex);
                        } catch (InvalidProtocolBufferException ex) {
                            log.error(ex);
                        }
                    }

                  });
                
                Object lock = new Object();
                synchronized (lock) {
                    lock.wait();
                }
                
                session.close();
            } catch (NamingException ex) {
                log.error(ex);
            } catch (JMSException ex) {
                log.error(ex);
            } catch (InterruptedException ex) {
                log.error(ex);
            } finally {
                if (connection != null) {
                    try {
                        connection.close();
                    } catch (JMSException ex) {
                        log.error(ex);
                    }
                }
            }
        };
        Thread thread = new Thread(runnable);
        thread.start();
    }
}

