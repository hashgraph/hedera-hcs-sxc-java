package com.hedera.hcslib.listeners;

import java.net.URISyntaxException;
import java.util.Hashtable;
import javax.jms.ConnectionFactory;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.jms.Topic;
import javax.jms.TopicSubscriber;
import javax.naming.Context;
import javax.naming.InitialContext;

/*
https://examples.javacodegeeks.com/enterprise-java/jms/jms-messagelistener-example/
*/

public class MQListenerService {
  public static void main(String[] args) throws URISyntaxException, Exception {
        InitialContext initialContext = null;
        
        javax.jms.Connection connection = null;
        
        try {
       
            
                /**
             * Create properties to connect to the hsc-queue / topic server
             * Note, these environment settings can be put it
             * src/main/resources/jndi.properties and use this format:
             *
             * java.naming.factory.initial=org.apache.activemq.artemis.jndi.ActiveMQInitialContextFactory
             * connectionFactory.ConnectionFactory=tcp://localhost:61616
             * topic.topic/exampleTopic=exampleTopic
             *
             * Then that file can be shared across different clients.
             */
            Hashtable<String, Object> props = new Hashtable<>();
            props.put(Context.INITIAL_CONTEXT_FACTORY, "org.apache.activemq.artemis.jndi.ActiveMQInitialContextFactory");
            props.put("topic.topic/exampleTopic", "exampleTopic");
            props.put("connectionFactory.VmConnectionFactory", "vm://0");
            props.put("connectionFactory.TCPConnectionFactory", "tcp://localhost:61616");
            //props.put("connectionFactory.UDPConnectionFactory", "udp://" + getUDPDiscoveryAddress() + ":" + getUDPDiscoveryPort());
            props.put("connectionFactory.JGroupsConnectionFactory", "jgroups://mychannelid?file=test-jgroups-file_ping.xml");
            InitialContext ctx = new InitialContext(props);
            ctx.lookup("VmConnectionFactory");
            ctx.lookup("TCPConnectionFactory");
            //ctx.lookup("UDPConnectionFactory");
            ctx.lookup("JGroupsConnectionFactory");

            // Step 1. Create an initial context to perform the JNDI lookup.
            initialContext = ctx;

            // Step 2. Look-up the JMS topic
            Topic topic = (Topic) initialContext.lookup("topic/exampleTopic");

            // Step 3. Look-up the JMS connection factory
            ConnectionFactory cf = (ConnectionFactory) initialContext.lookup("TCPConnectionFactory");

            // Step 4. Create a JMS connection
            connection = cf.createConnection();

            // Step 5. Set the client-id on the connection
            connection.setClientID("durable-client");

            // Step 6. Start the connection
           

            // Step 7. Create a JMS session
            Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);

            // Step 8. Create a JMS message producer
            //MessageProducer messageProducer = session.createProducer(topic);

            // Step 9. Create the subscription and the subscriber.
            TopicSubscriber subscriber = session.createDurableSubscriber(topic, "subscriber-1");
            subscriber.setMessageListener(new MQListener(true));
     
    
             
        
           //connection.start();     
             
            Thread.sleep(1000);
             
            System.out.println("Change the message listener to acknowledge");
            // System.out.println("Sending text '" + payload + "'");
            //
            //consumer.setMessageListener(
            //        new AckMessageListener(true));
             
            Thread.sleep(1000);
            session.close();
        } finally {
            if (connection != null) {
                connection.close();
            }
            //broker.stop();
        }
    }
}

