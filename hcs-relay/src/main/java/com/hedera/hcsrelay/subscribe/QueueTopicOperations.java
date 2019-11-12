package com.hedera.hcsrelay.subscribe;

import com.hedera.hashgraph.sdk.consensus.TopicId;
import com.hedera.hashgraph.sdk.proto.TopicID;
import com.hedera.hcslib.messages.HCSRelayMessage;
import com.hedera.hcsrelay.config.Config;
import com.hedera.hcsrelay.config.Queue;
import com.hedera.mirror.api.proto.java.MirrorGetTopicMessages;

import lombok.extern.log4j.Log4j2;

import java.util.Hashtable;
import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.JMSException;
import javax.jms.MessageProducer;
import javax.jms.ObjectMessage;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.jms.Topic;
import javax.jms.TopicSubscriber;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

/**
 *
 * @author User
 */
@Log4j2
public class QueueTopicOperations {
   
    /**
     * Sets up jms topic.It blocks until the jms queue becomes available. 
     * @param config
     * @param topicNum
     * @return true if topic created and communication succeeded.
     * @throws JMSException
     * @throws NamingException 
     */
    public static boolean blockingCreateJmsTopic(Config config) throws JMSException, NamingException {
        //JMS config
        Connection connection = null;
        InitialContext initialContext = null;
        boolean r = false;
        
        try {
            
            Queue queueConfig = config.getConfig().getQueue();
            
            Hashtable<String, Object> props = new Hashtable<>();
            props.put(Context.INITIAL_CONTEXT_FACTORY, queueConfig.getInitialContextFactory());
            props.put("topic.topic/hcsTopic",  "hcsCatchAllTopics");
            
            props.put("connectionFactory.TCPConnectionFactory", queueConfig.getTcpConnectionFactory());
            InitialContext ctx = new InitialContext(props);
            ctx.lookup("TCPConnectionFactory");

            // Step 1. Create an initial context to perform the JNDI lookup.
            initialContext = ctx;

            // Step 2. Look-up the JMS topic
            Topic topic = (Topic) initialContext.lookup("topic/hcsTopic");

            // Step 3. Look-up the JMS connection factory
            ConnectionFactory cf = (ConnectionFactory) initialContext.lookup("TCPConnectionFactory");

            // Step 4. Create a JMS connection and wait until server available
            log.info("Waiting for MQ Artemis to start ...");
            
            boolean scanning = true;
            do {
                try {
                    connection = cf.createConnection();
                    scanning = false;
                } catch (Exception ie) {
                    String tcpConnectionFactory = queueConfig.getTcpConnectionFactory();
                    log.info("Is Artemis up? Setup your host file so that the host identified in'"+tcpConnectionFactory+"' points to 127.0.0.1 if running outside of docker");
                    Thread.sleep(6000);
                }
                
            } while (scanning);

            // Step 5. Set the client-id on the connection
            connection.setClientID("topic-setup-relay:hcsCatchAllTopics");

            // Step 6. Start the connection
            connection.start();

            // Step 7. Create a JMS session
            Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);

            // Step 8. Create a JMS message producer
            MessageProducer messageProducer = session.createProducer(topic);

            // Step 9. Create the subscription and the first test subscriber (he is removed at the end of the test).
            // TopicSubscriber subscriber = session.createDurableSubscriber(topic, "text-subscriber-topic-hcsCatchAllTopics");

            // Step 10. Create a text message
            //TextMessage message1 = session.createTextMessage("Test queue message on topic hcsCatchAllTopics" );

            // Step 11. Send the text message to the topic
            //messageProducer.send(message1);

            //log.info("Sent test queue message: " + message1.getText() + " from producer hcsCatchAllTopics" );

            // Step 12. Consume the message from the durable subscription
            //TextMessage messageReceived = (TextMessage) subscriber.receive();

            //log.info("Received message from queue from test subscriber. Message: " + messageReceived.getText());
            
            //subscriber.close();

            // Step 13. Delete the durable subscription
            //session.unsubscribe("text-subscriber-topic-hcsCatchAllTopics");
            r = true;
            
        } catch (Exception e) {
            e.printStackTrace();;
        } finally {
            if (connection != null) {
                connection.close();
            }
            if (initialContext != null) {
                initialContext.close();
            }
        }
         return r;
    }

    public static void addMessage(Config config, MirrorGetTopicMessages.MirrorGetTopicMessagesResponse messagesResponse, TopicId topicId) throws JMSException, NamingException {
        long topicNum = topicId.getTopicNum();
        Connection connection = null;
        InitialContext initialContext = null;
        boolean r = false;
        
        try {
            Queue queueConfig = config.getConfig().getQueue();
            Hashtable<String, Object> props = new Hashtable<>();
            props.put(Context.INITIAL_CONTEXT_FACTORY, queueConfig.getInitialContextFactory());
            props.put("topic.topic/hcsTopic",  "hcsCatchAllTopics");
            props.put("connectionFactory.TCPConnectionFactory", queueConfig.getTcpConnectionFactory());
            InitialContext ctx = new InitialContext(props);
            ctx.lookup("TCPConnectionFactory");
            // Step 1. Create an initial context to perform the JNDI lookup.
            initialContext = ctx;

            // Step 2. Look-up the JMS topic
            Topic topic = (Topic) initialContext.lookup("topic/hcsTopic");

            // Step 3. Look-up the JMS connection factory
            ConnectionFactory cf = (ConnectionFactory) initialContext.lookup("TCPConnectionFactory");

            // Step 4. Create a JMS connection and wait until server available
            //log.info("Waiting for MQ Artemis to start ...");
            
            boolean scanning = true;
            do {
                try {
                    connection = cf.createConnection();
                    scanning = false;
                } catch (Exception ie) {
                    String tcpConnectionFactory = config.getConfig().getQueue().getTcpConnectionFactory();
                    log.info("Is Artemis up? Setup your host file so that the host identified in'"+tcpConnectionFactory+"' points to 127.0.0.1 if running outside of docker");
                    Thread.sleep(6000);
                }
                
            } while (scanning);

            // Step 5. Set the client-id on the connection
            connection.setClientID("topic-setup-relay:"+topicNum);

            // Step 6. Start the connection
            connection.start();

            // Step 7. Create a JMS session
            Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);

            // Step 8. Create a JMS message producer
            MessageProducer messageProducer = session.createProducer(topic);

            // Step 9. Create the subscription and the first test subscriber (he is removed at the end of the test).
            //TopicSubscriber subscriber = session.createDurableSubscriber(topic, "text-subscriber-topic-"+topicNum);
            // Step 10. Create a message
            
            HCSRelayMessage relayMessage = new HCSRelayMessage(
                    messagesResponse, 
                    TopicID.newBuilder()
                            .setRealmNum(topicId.getTopicNum())
                            .setShardNum(topicId.getShardNum())
                            .setTopicNum(topicId.getTopicNum())
                            .build()
            );
            ObjectMessage objectMessage = session.createObjectMessage(relayMessage);
           
            // Step 11. Send the text message to the topic
            messageProducer.send(objectMessage);
        
            r = true;
            
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (connection != null) {
                connection.close();
            }
            if (initialContext != null) {
                initialContext.close();
            }
        }
    }
}
