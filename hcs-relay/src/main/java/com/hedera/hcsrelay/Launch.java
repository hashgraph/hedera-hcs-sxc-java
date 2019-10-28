package com.hedera.hcsrelay;

import com.hedera.hcsrelay.config.Config;
import com.hedera.hcsrelay.config.Queue;
import com.hedera.hcsrelay.subscribe.MirrorTopicsSubscriber;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.Hashtable;
import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.jms.Topic;
import javax.jms.TopicSubscriber;
import javax.naming.Context;
import javax.naming.InitialContext;
import sun.net.ConnectionResetException;

public final class Launch {

    public static void main(String[] args) throws Exception {

        Config config = new Config();
        MirrorTopicsSubscriber topicsSubscriber = new MirrorTopicsSubscriber();
        
        System.out.println("Relay started");

    }

    

}
