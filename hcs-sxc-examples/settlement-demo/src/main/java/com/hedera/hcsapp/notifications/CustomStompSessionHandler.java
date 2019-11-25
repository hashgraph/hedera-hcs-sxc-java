package com.hedera.hcsapp.notifications;

import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaders;
import org.springframework.messaging.simp.stomp.StompSession;
import org.springframework.messaging.simp.stomp.StompSessionHandlerAdapter;

import lombok.extern.log4j.Log4j2;

/**
 * This class is an implementation for <code>StompSessionHandlerAdapter</code>.
 */

@Log4j2
public class CustomStompSessionHandler extends StompSessionHandlerAdapter {

    @Override
    public void afterConnected(StompSession session, StompHeaders connectedHeaders) {
        log.info("New websocket session established : " + session.getSessionId());
    }

    @Override
    public void handleException(StompSession session, StompCommand command, StompHeaders headers, byte[] payload, Throwable exception) {
        log.error("Got an exception", exception);
    }
    
    @Override
    public void handleFrame(StompHeaders headers, Object payload) {
        NotificationMessage msg = (NotificationMessage) payload;
//      log.info("Received : " + msg.getPayer() + ", " + msg.getRecipient() + ", " + msg.getThreadId());
      log.info("Received : " + msg.getRecipient());
    }    
}
