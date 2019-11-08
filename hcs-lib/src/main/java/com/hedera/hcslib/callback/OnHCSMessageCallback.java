package com.hedera.hcslib.callback;

import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import com.hedera.hcslib.HCSLib;
import com.hedera.hcslib.interfaces.LibMessagePersistence;

import com.hedera.hcslib.messages.HCSRelayMessage;
import com.hedera.hcslib.proto.java.MessageEnvelope;
import com.hedera.hcslib.proto.java.MessagePart;
import com.hedera.hcslib.proto.java.TransactionID;
import com.hedera.hcslib.utils.ByteUtil;
import io.github.classgraph.ClassGraph;
import io.github.classgraph.ClassInfo;
import io.github.classgraph.ClassInfoList;
import io.github.classgraph.ScanResult;

import lombok.extern.log4j.Log4j2;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
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
import lombok.NoArgsConstructor;
import org.apache.activemq.artemis.jms.client.ActiveMQObjectMessage;
import org.apache.activemq.artemis.jms.client.ActiveMQTextMessage;
import org.apache.commons.lang3.tuple.Pair;

/**
 * 
 * Implements callback registration and notification capabilities to support apps
 *
 */
@Log4j2
public final class OnHCSMessageCallback {
    
 
    LibMessagePersistence persistence;
    
    
    public OnHCSMessageCallback (HCSLib hcsLib) throws ClassNotFoundException, InstantiationException, IllegalAccessException {
        // load persistence implementation at runtime
        try (ScanResult result = new ClassGraph().enableAllInfo()
                .whitelistPackages("com.hedera.plugin.persistence.inmemory")
                .scan()) {
            ClassInfoList list = result.getAllClasses();
            persistence = (LibMessagePersistence)list.get(0).loadClass().newInstance();
        }
        
        
        String jmsAddress = hcsLib.getJmsAddress();
        Runnable runnable;
        runnable = () -> { 
            InitialContext initialContext = null;
            javax.jms.Connection connection = null;
            try {
                System.out.println("Starting hcs topic listener in hcs-lib");
                Hashtable<String, Object> props = new Hashtable<>();
                props.put(Context.INITIAL_CONTEXT_FACTORY, "org.apache.activemq.artemis.jndi.ActiveMQInitialContextFactory");
                props.put("topic.topic/hcsTopic", "hcsCatchAllTopics");
                props.put("connectionFactory.TCPConnectionFactory", jmsAddress);
                InitialContext ctx = new InitialContext(props);
                ctx.lookup("TCPConnectionFactory");

                // Step 1. Create an initial context to perform the JNDI lookup.
                initialContext = ctx;

                // Step 2. Look-up the JMS topic
                Topic topic = (Topic) initialContext.lookup("topic/hcsTopic");

                // Step 3. Look-up the JMS connection factory
                ConnectionFactory cf = (ConnectionFactory) initialContext.lookup("TCPConnectionFactory");

                // Step 4. Create a JMS connection
                connection = cf.createConnection();

                // Step 5. Set the client-id on the connection
                connection.setClientID("operator-client-"+hcsLib.getOperatorAccountId().getAccountNum());

                // Step 6. Start the connection
                connection.start();

                // Step 7. Create a JMS session
                Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);

                // Step 8. Create a JMS message producer
                //MessageProducer messageProducer = session.createProducer(topic);

                // Step 9. Create the subscription and the subscriber.

                TopicSubscriber subscriber = session.createDurableSubscriber(topic, "subscriber-hcsCatchAllTopics-in-lib");

                //for synchronus receive do
                //Message receive = subscriber.receive();
                //log.info(((javax.jms.TextMessage)receive).getText());

                //for aync receive do
                subscriber.setMessageListener(new MessageListener() {
                    @Override
                    public void onMessage(Message messageFromJMS) {
                        try {
                            log.info("Message Received from JMS forward to app.java observers");
                            // notify subscribed observer from App.java
                            if (messageFromJMS instanceof ActiveMQTextMessage) {
                                OnHCSMessageCallback.this.notifyObservers(((ActiveMQTextMessage)messageFromJMS).getText());
                                messageFromJMS.acknowledge();
                            } else if (messageFromJMS instanceof ActiveMQObjectMessage) {
                                HCSRelayMessage rlm = (HCSRelayMessage)((ActiveMQObjectMessage) messageFromJMS).getObject();
                                ByteString message = rlm.getTopicMessagesResponse().getMessage();
                                MessagePart messagePart = MessagePart.parseFrom(message);
                                
                                Optional<MessageEnvelope> messageEnvelopeOptional = 
                                        pushUntilCompleteMessage(messagePart, persistence);
                                if (messageEnvelopeOptional.isPresent()){
                                    OnHCSMessageCallback.this.notifyObservers("The object received queue is complete = "+  messageEnvelopeOptional.get().getMessageEnvelope().toStringUtf8());
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
                
                
                //consumer.setMessageListener(
                //        new AckMessageListener(true));

                //Thread.sleep(1000);
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
                //broker.stop();
            }
        };
        Thread thread = new Thread(runnable);
        thread.start();
    }
    
    
    
    private final List<HCSCallBackInterface> observers = new ArrayList<>();

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
     * Adds messageParts / chunks into memory {@param partialMessages}  and returns
     * a fully combined / assembled MessageEnvleope if all parts are present
     * @param messagePart a chunked message received from the queue
     * @param persistence the memory. The object is side-effected with each 
     * function invocation. 
     * @return a fully combined / assembled MessageEnvleope if all parts present, 
     * nothing otherwise.
     * @throws InvalidProtocolBufferException 
     */
    public static  Optional<MessageEnvelope> pushUntilCompleteMessage(MessagePart messagePart, LibMessagePersistence persistence) throws InvalidProtocolBufferException {
            
            TransactionID messageEnvelopeId = messagePart.getMessageEnvelopeId();
            //look up db to find parts received already
            List<MessagePart> partsList = persistence.getParts(messageEnvelopeId);
            // if first time seen
            if (partsList == null){
                // if it's a single part message return it to app
                if(messagePart.getPartsTotal() == 1){
                    MessageEnvelope messageEnvelope = MessageEnvelope.parseFrom(messagePart.getMessagePart());
                    return  Optional.of( messageEnvelope);

                } else { // it's the first of a multipart message - order does not matter
                    List l = new ArrayList<>();
                    l.add(messagePart);
                    persistence.putParts(messageEnvelopeId,l);
                }

            } else { // there are some parts received already
                partsList.add(messagePart);
                // if all parts received
                if (partsList.size() == messagePart.getPartsTotal()) {
                    // sort by part id
                    partsList.sort(Comparator.comparingInt(MessagePart::getPartId));
                    // merge down
                    ByteString merged =
                            partsList.stream()
                                    .map(MessagePart::getMessagePart)
                                    .reduce(ByteUtil::merge).get();
                    // construct envelope from merged array. TODO: if fail
                    MessageEnvelope messageEnvelope = MessageEnvelope.parseFrom(merged);
                    persistence.removeParts(messageEnvelopeId);
                    return  Optional.of(messageEnvelope);
                }  else { // not all parts received yet
                    persistence.putParts(messageEnvelopeId, partsList);
                }

            }
            return Optional.empty();
        }
}
