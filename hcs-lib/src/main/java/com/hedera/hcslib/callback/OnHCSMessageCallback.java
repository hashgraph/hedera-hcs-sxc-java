package com.hedera.hcslib.callback;

import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import com.hedera.hcslib.HCSLib;
import com.hedera.hcslib.interfaces.LibMessagePersistence;
import com.hedera.hcslib.interfaces.MessagePersistenceLevel;

import com.hedera.hcslib.messages.HCSRelayMessage;
import com.hedera.hcslib.plugins.Plugins;
import com.hedera.hcslib.proto.java.ApplicationMessage;
import com.hedera.hcslib.proto.java.ApplicationMessageChunk;
import com.hedera.hcslib.proto.java.TransactionID;
import com.hedera.hcslib.utils.ByteUtil;
import lombok.extern.log4j.Log4j2;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Hashtable;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
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

/**
 * 
 * Implements callback registration and notification capabilities to support apps
 *
 */
@Log4j2
public final class OnHCSMessageCallback {
    
 
    LibMessagePersistence persistence;
    private final List<HCSCallBackInterface> observers = new ArrayList<>();

    public OnHCSMessageCallback (HCSLib hcsLib) throws Exception {
        // load persistence implementation at runtime
        Class<?> persistenceClass = Plugins.find("com.hedera.plugin.persistence.*", "com.hedera.hcslib.interfaces.LibMessagePersistence", true);
        persistence = (LibMessagePersistence)persistenceClass.newInstance();

        String contextFactory = hcsLib.getInitialContextFactory();
        String tcpConnectionFactory = hcsLib.getTCPConnectionFactory();
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
                        log.info("Unable to connect to message queue - sleeping 5s");
                        TimeUnit.SECONDS.sleep(5);
                    }
                }

                connection.setClientID("operator-client-" + hcsLib.getApplicationId());

                connection.start();

                Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);

                TopicSubscriber subscriber = session.createDurableSubscriber(topic, "subscriber-hcsCatchAllTopics-in-lib");

                subscriber.setMessageListener(new MessageListener() {
                    @Override
                    public void onMessage(Message messageFromJMS) {
                        try {
                            // notify subscribed observer from App.java
                            if (messageFromJMS instanceof ActiveMQTextMessage) {
                                OnHCSMessageCallback.this.notifyObservers(((ActiveMQTextMessage)messageFromJMS).getText());
                                messageFromJMS.acknowledge();
                            } else if (messageFromJMS instanceof ActiveMQObjectMessage) {
                                HCSRelayMessage rlm = (HCSRelayMessage)((ActiveMQObjectMessage) messageFromJMS).getObject();
                                persistence.storeMessage(MessagePersistenceLevel.NONE, rlm.getTopicMessagesResponse().toBuilder());
                                   
                                ByteString message = rlm.getTopicMessagesResponse().getMessage();
                                ApplicationMessageChunk messagePart = ApplicationMessageChunk.parseFrom(message);
                                
                                Optional<ApplicationMessage> messageEnvelopeOptional = 
                                        pushUntilCompleteMessage(messagePart, persistence);
                                if (messageEnvelopeOptional.isPresent()){
                                    OnHCSMessageCallback.this.notifyObservers( messageEnvelopeOptional.get().getBusinessProcessMessage().toStringUtf8());
                                    messageFromJMS.acknowledge();
                                }
                            }
                            
                        } catch (JMSException ex) {
                            log.error(ex);
                        } catch (InvalidProtocolBufferException ex) {
                            Logger.getLogger(OnHCSMessageCallback.class.getName()).log(Level.SEVERE, null, ex);
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
    
    /**
     * Adds an observer to the list of observers
     * @param listener
     */
    public void addObserver(HCSCallBackInterface listener) {
       observers.add(listener);
    }
    /**
     * Notifies all observers with the supplied message
     * @param message
     */
    void notifyObservers(String message){
        observers.forEach(listener -> listener.onMessage(message));
    }
    
    /**
     * Adds ApplicationMessageChunk into memory and returns
     * a fully combined / assembled ApplicationMessage if all parts are present
     * @param messageChunk a chunked message received from the queue
     * @param persistence the memory. The object is side-effected with each 
     * function invocation. 
     * @return a fully combined / assembled ApplicationMessage if all parts present, 
     * nothing otherwise.
     * @throws InvalidProtocolBufferException 
     */
    public static  Optional<ApplicationMessage> pushUntilCompleteMessage(ApplicationMessageChunk messageChunk, LibMessagePersistence persistence) throws InvalidProtocolBufferException {
            
        TransactionID applicationMessageId = messageChunk.getApplicationMessageId();
        //look up db to find parts received already
        List<ApplicationMessageChunk> chunkList = persistence.getParts(applicationMessageId);
        if(chunkList==null){
            chunkList = new ArrayList<ApplicationMessageChunk>();
            chunkList.add(messageChunk);
        } else {
            chunkList.add(messageChunk);
        }
        persistence.putChunks(applicationMessageId, chunkList);
        
        if(messageChunk.getChunksCount() == 1){
                ApplicationMessage applicationMessage = ApplicationMessage.parseFrom(messageChunk.getMessageChunk());
                return  Optional.of( applicationMessage);
        } else if (chunkList.size() == messageChunk.getChunksCount()) { // all parts received
                // sort by part id
                chunkList.sort(Comparator.comparingInt(ApplicationMessageChunk::getChunkIndex));
                // merge down
                ByteString merged =
                        chunkList.stream()
                                .map(ApplicationMessageChunk::getMessageChunk)
                                .reduce(ByteUtil::merge).get();
                // construct envelope from merged array. TODO: if fail
                ApplicationMessage messageEnvelope = ApplicationMessage.parseFrom(merged);
                return  Optional.of(messageEnvelope);
        } else { // not all parts received yet
            return Optional.empty(); 
        }
    }
}
