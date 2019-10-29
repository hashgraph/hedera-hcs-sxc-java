package com.hedera.hcsrelay.subscribe;
import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import com.hedera.hashgraph.sdk.consensus.TopicId;
import com.hedera.hashgraph.sdk.proto.ConsensusSubmitMessageTransactionBody;
import com.hedera.hashgraph.sdk.proto.ResponseCodeEnum;
import com.hedera.hashgraph.sdk.proto.TopicID;
import com.hedera.hashgraph.sdk.proto.Transaction;
import com.hedera.hashgraph.sdk.proto.TransactionBody;
import com.hedera.hashgraph.sdk.proto.TransactionReceipt;
import com.hedera.hashgraph.sdk.proto.TransactionRecord;
import com.hedera.hcsrelay.config.Config;
import com.hedera.hcsrelay.config.Queue;
import java.util.Hashtable;
import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.JMSException;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.jms.Topic;
import javax.jms.TopicSubscriber;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

/**
 * Subscribes to topic(s) against a mirror node
 * Listens to mirror messages, filters by topic and forwards to mq
 */
public final class MirrorTopicsSubscriber {
    
    Config config = new Config();
    
    public MirrorTopicsSubscriber() throws Exception {
        
        
        String mirrorAddress = config.getConfig().getMirrorAddress();
        
        
        for (TopicId topic : config.getConfig().getTopicIds()) {
                System.out.println("Subscribing to topic number " + topic.getTopicNum());

                System.out.println("Seting up MQ Artemis to topic number " + topic.getTopicNum());
                this.setupJmsTopic(config, topic.getTopicNum());
              
                // send subscription request to mirrorAddress for topic using the SDK
                // likely needs a callback method       
        }
        
    }

    /**
     * TODO implement jax-rs POST or similar
     * @param txRcd
     * @param txRcp 
     */
     public void onMirrorMessage(Transaction transaction, TransactionRecord txRecord) throws InvalidProtocolBufferException{
            //step 1 filter topics
            TransactionBody body = null;
            if (transaction.hasBody()) {
                    body = transaction.getBody();
            } else {
                    body = TransactionBody.parseFrom(transaction.getBodyBytes());
            }

        
            TransactionReceipt receipt = txRecord.getReceipt();
            
            if (receipt.getStatus() == ResponseCodeEnum.SUCCESS) {
                    // transaction was successful
            } else {
                    // transaction failed for some reason.
            }

            if (body.hasConsensusCreateTopic()){

            } else if (body.hasConsensusDeleteTopic()){

            } else if (body.hasConsensusSubmitMessage()){
                ConsensusSubmitMessageTransactionBody consensusSubmitMessage = body.getConsensusSubmitMessage();
                TopicID topicID = consensusSubmitMessage.getTopicID();
                long realmNum = topicID.getRealmNum();
                long shardNum = topicID.getShardNum();
                long topicNum = topicID.getTopicNum();  
                TopicId  topicId = new TopicId(shardNum, realmNum, topicNum);
                
                Long consensusTimeStampSeconds = txRecord.getConsensusTimestamp().getSeconds();
                int consensusTimeStampNanos = txRecord.getConsensusTimestamp().getNanos();
                ByteString message = consensusSubmitMessage.getMessage();

                // filter topic and push tx to mq
                
                if (config.getConfig().getTopicIds().contains(topicId)){
                    
                }

            } else if (body.hasConsensusUpdateTopic()){

            } else {

            }
                
            
    }
    
    private void setupJmsTopic(Config config, long topicNum) throws JMSException, NamingException {
        //JMS config
        Connection connection = null;
        InitialContext initialContext = null;
        
        try {
            
            Queue queueConfig = config.getConfig().getQueue();
            
            Hashtable<String, Object> props = new Hashtable<>();
            props.put(Context.INITIAL_CONTEXT_FACTORY, queueConfig.getInitialContextFactory());
            props.put("topic.topic/hcsTopic",  "0.0."+topicNum);
            
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
            System.out.println("Waiting for MQ Artemis to start ...");
            
            boolean scanning = true;
            do {
                try {
                    connection = cf.createConnection();
                    scanning = false;
                } catch (Exception ie) {
                    System.out.println("Is Artemis up? Is the hostname hcsqueue? Point your hosts file to hcsqueue if running outside of docker");
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
             TopicSubscriber subscriber = session.createDurableSubscriber(topic, "text-subscriber-topic-"+topicNum);

            // Step 10. Create a text message
            TextMessage message1 = session.createTextMessage("Test message on topic " + topicNum);

            // Step 11. Send the text message to the topic
            messageProducer.send(message1);

            System.out.println("Sent message: " + message1.getText() + "prducer " + topicNum);

            // Step 12. Consume the message from the durable subscription
            TextMessage messageReceived = (TextMessage) subscriber.receive();

            System.out.println("Received message: " + messageReceived.getText());
            
            subscriber.close();

            // Step 13. Delete the durable subscription
            session.unsubscribe("text-subscriber-topic-"+topicNum);
            
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
    }


    private void postToJmsTopic(Config config, long topicNum, Object message) {
        
    }

}
