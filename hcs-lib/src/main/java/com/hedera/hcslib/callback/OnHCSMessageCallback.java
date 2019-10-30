package com.hedera.hcslib.callback;

import com.hedera.hcslib.config.Config;
import com.hedera.hcslib.listeners.MQListener;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.jms.ConnectionFactory;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Session;
import javax.jms.Topic;
import javax.jms.TopicSubscriber;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

/**
 * 
 * Implements callback registration and notification capabilities to support apps
 *
 */
public final class OnHCSMessageCallback {
    
    public OnHCSMessageCallback (Config config) {
       
        Runnable runnable =
            () -> { 
            // use config from app to setup  mq
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
                //Message receive = subscriber.receive();
                //System.out.print(((javax.jms.TextMessage)receive).getText());

                //for aync receive do
                subscriber.setMessageListener(
                        messageFromJMS -> {
                                try {
                                    System.out.println("Message Received from JMS forward to app.java observers");
                                    this.notifyObservers(((javax.jms.TextMessage)messageFromJMS).getText());
                                    messageFromJMS.acknowledge();
                                } catch (JMSException ex) {
                                    Logger.getLogger(OnHCSMessageCallback.class.getName()).log(Level.SEVERE, null, ex);
                                }
                        }
                );

               Object lock = new Object();
               synchronized (lock) {
                   lock.wait();
               }


                //consumer.setMessageListener(
                //        new AckMessageListener(true));

                //Thread.sleep(1000);
                session.close();
            } catch (NamingException ex) {
                Logger.getLogger(OnHCSMessageCallback.class.getName()).log(Level.SEVERE, null, ex);
            } catch (JMSException ex) {
                Logger.getLogger(OnHCSMessageCallback.class.getName()).log(Level.SEVERE, null, ex);
            } catch (InterruptedException ex) {
                Logger.getLogger(OnHCSMessageCallback.class.getName()).log(Level.SEVERE, null, ex);
            } finally {
                if (connection != null) {
                    try {
                        connection.close();
                    } catch (JMSException ex) {
                        Logger.getLogger(OnHCSMessageCallback.class.getName()).log(Level.SEVERE, null, ex);
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
     * For test purposes for now
     */
    //TODO: Remove this
    //public void triggerCallBack() {
    //    notifyObservers("hi there");
   //}
    
}
