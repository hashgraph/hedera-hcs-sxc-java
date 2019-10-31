package com.hedera.hcslib.listeners;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;

import lombok.extern.log4j.Log4j2;

/*
https://examples.javacodegeeks.com/enterprise-java/jms/jms-messagelistener-example/
*/

@Log4j2
public class MQListener implements MessageListener{
    private boolean acknowledge;
    
    public MQListener(boolean acknowledge) {
        this.acknowledge = acknowledge;
    }
 
    @Override
    public void onMessage(Message message) {
        log.info("Message received from queue in MQListener.java");
        if (acknowledge) {
            try {
               
                
                message.acknowledge();
            } catch (JMSException e1) {
                e1.printStackTrace();
            }
        }
        log.info(message);
    }
}

