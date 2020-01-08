package com.hedera.hcssxcrelay.subscribe;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.Hashtable;
import java.util.concurrent.TimeUnit;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.JMSException;
import javax.jms.MessageProducer;
import javax.jms.ObjectMessage;
import javax.jms.Session;
import javax.jms.Topic;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import com.hedera.hashgraph.sdk.consensus.ConsensusMessage;
import com.hedera.hashgraph.sdk.consensus.ConsensusTopicId;
import com.hedera.hcslib.interfaces.HCSRelayMessage;
import com.hedera.hcssxcrelay.config.Config;
import com.hedera.hcssxcrelay.config.Queue;

import lombok.extern.log4j.Log4j2;

@Log4j2
public final class MirrorMessageHandler {


    public static void onMirrorMessage(ConsensusMessage messagesResponse, ConsensusTopicId topicId) {
        try {
            Config config = new Config();

            log.info("Got message from mirror node");
            log.info(messagesResponse.toString());

            addMessage(config, messagesResponse, topicId);
            log.info("Message added to queue");
        } catch (Exception ex) {
            log.error(ex);
        }
    }

    private static void addMessage(Config config, ConsensusMessage messagesResponse, ConsensusTopicId topicId) throws JMSException, NamingException {
        long topicNum = topicId.topic;
        Connection connection = null;
        InitialContext initialContext = null;

        try {
            log.info("Sending message to queue");
            Queue queueConfig = config.getConfig().getQueue();
            Hashtable<String, Object> props = new Hashtable<>();
            props.put(Context.INITIAL_CONTEXT_FACTORY, queueConfig.getInitialContextFactory());
            props.put("topic.topic/hcsTopic",  "hcsCatchAllTopics");
            props.put("connectionFactory.TCPConnectionFactory", queueConfig.getTcpConnectionFactory());
            InitialContext ctx = new InitialContext(props);
            ctx.lookup("TCPConnectionFactory");

            initialContext = ctx;

            Topic topic = (Topic) initialContext.lookup("topic/hcsTopic");

            ConnectionFactory cf = (ConnectionFactory) initialContext.lookup("TCPConnectionFactory");

            boolean scanning = true;
            do {
                try {
                    connection = cf.createConnection();
                    scanning = false;
                } catch (Exception ie) {
                    String tcpConnectionFactory = config.getConfig().getQueue().getTcpConnectionFactory();
                    log.info("Is Artemis up? Setup your host file so that the host identified in'"+tcpConnectionFactory+"' points to 127.0.0.1 if running outside of docker");
                    TimeUnit.SECONDS.sleep(6000);
                }

            } while (scanning);

            connection.setClientID("topic-setup-relay:"+topicNum);

            connection.start();

            Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);

            MessageProducer messageProducer = session.createProducer(topic);

            HCSRelayMessage relayMessage = new HCSRelayMessage(messagesResponse);

            ObjectMessage objectMessage = session.createObjectMessage(relayMessage);

            messageProducer.send(objectMessage);

            // store last consensus timestamp
            try(BufferedWriter br = new BufferedWriter(new FileWriter(config.getConfig().getLastConsensusTimeFile()))) {
                long seconds = relayMessage.getConsensusTimestamp().getEpochSecond();
                int nanos = relayMessage.getConsensusTimestamp().getNano();
                br.write(seconds + "-" + nanos);
            }

            log.info("Sent message to queue");

        } catch (Exception e) {
            log.error(e);
        } finally {
            if (connection != null) {
                log.info("addMessage - Closing JMS connection");
                connection.close();
            }
            if (initialContext != null) {
                log.info("addMessage- Closing JMS initial context");
                initialContext.close();
            }
        }
    }
}
