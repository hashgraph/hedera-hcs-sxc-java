package hcsrelay;

import java.util.Hashtable;
import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.jms.Topic;
import javax.jms.TopicSubscriber;
import javax.naming.InitialContext;

import javax.naming.Context;

/**
 * Hello world!
 *
 */
public class Main {

    public static void main(final String[] args) throws Exception {
        Connection connection = null;
        InitialContext initialContext = null;
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
            connection.setClientID("durable-client-relay");

            // Step 6. Start the connection
            connection.start();

            // Step 7. Create a JMS session
            Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);

            // Step 8. Create a JMS message producer
            MessageProducer messageProducer = session.createProducer(topic);

            // Step 9. Create the subscription and the subscriber.
            TopicSubscriber subscriber = session.createDurableSubscriber(topic, "subscriber-1");

            // Step 10. Create a text message
            TextMessage message1 = session.createTextMessage("This is a text message 1");

            // Step 11. Send the text message to the topic
            messageProducer.send(message1);

            System.out.println("Sent message: " + message1.getText());

            // Step 12. Consume the message from the durable subscription
            TextMessage messageReceived = (TextMessage) subscriber.receive();

            System.out.println("Received message: " + messageReceived.getText());

            // Step 13. Create and send another message
            TextMessage message2 = session.createTextMessage("This is a text message 2");

            messageProducer.send(message2);

            System.out.println("Sent message: " + message2.getText());

            // Step 14. Close the subscriber - the server could even be stopped at this point!
            subscriber.close();

            // Step 15. Create a new subscriber on the *same* durable subscription.
            subscriber = session.createDurableSubscriber(topic, "useless-subscriber-in-relay");

            // Step 16. Consume the message
            messageReceived = (TextMessage) subscriber.receive();

            System.out.println("Received message: " + messageReceived.getText());

            // Step 17. Close the subscriber
            subscriber.close();

            // Step 18. Delete the durable subscription
            session.unsubscribe("subscriber-1");
        } catch (Exception e) {
            e.printStackTrace();;
        } finally {
            if (connection != null) {
                // Step 19. Be sure to close our JMS resources!
                connection.close();
            }
            if (initialContext != null) {
                // Step 20. Also close the initialContext!
                initialContext.close();
            }
        }
    }
}
