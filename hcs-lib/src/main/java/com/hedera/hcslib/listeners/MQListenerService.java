package com.hedera.hcslib.listeners;

import java.net.URISyntaxException;
import java.util.Hashtable;
import javax.jms.ConnectionFactory;
import javax.jms.Message;
import javax.jms.Session;
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
            System.out.println("Starting hcs topic listener in hcs-lib");
            
            Hashtable<String, Object> props = new Hashtable<>();
            props.put(Context.INITIAL_CONTEXT_FACTORY, "org.apache.activemq.artemis.jndi.ActiveMQInitialContextFactory");
            props.put("topic.topic/hcsTopic", "hcsTopic");
            props.put("connectionFactory.TCPConnectionFactory", "tcp://localhost:61616");
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
            connection.setClientID("durable-client-hcs-lib");

            // Step 6. Start the connection
            connection.start();
            
            // Step 7. Create a JMS session
            Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);

            // Step 8. Create a JMS message producer
            //MessageProducer messageProducer = session.createProducer(topic);

            // Step 9. Create the subscription and the subscriber.
            
            TopicSubscriber subscriber = session.createDurableSubscriber(topic, "subscriber-1-in-lib");
            
            //for synchronus receive do
            Message receive = subscriber.receive();
            System.out.print(((javax.jms.TextMessage)receive).getText());
            
            //for aync receive do
            subscriber.setMessageListener(new MQListener(true));
     
    
           Object lock = new Object();
           synchronized (lock) {
               lock.wait();
           }
        
     
            //consumer.setMessageListener(
            //        new AckMessageListener(true));
             
            //Thread.sleep(1000);
            session.close();
        } finally {
            if (connection != null) {
                connection.close();
            }
            //broker.stop();
        }
    }
}

