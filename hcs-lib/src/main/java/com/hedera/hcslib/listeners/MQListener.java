package com.hedera.hcslib.listeners;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;

/*
https://examples.javacodegeeks.com/enterprise-java/jms/jms-messagelistener-example/
*/

public class MQListener implements MessageListener{
    private boolean acknowledge;
    
    public MQListener(boolean acknowledge) {
        this.acknowledge = acknowledge;
    }
 
    @Override
    public void onMessage(Message message) {
        if (acknowledge) {
            try {
                message.acknowledge();
            } catch (JMSException e1) {
                e1.printStackTrace();
            }
        }
        System.out.println(message);
    }
}

