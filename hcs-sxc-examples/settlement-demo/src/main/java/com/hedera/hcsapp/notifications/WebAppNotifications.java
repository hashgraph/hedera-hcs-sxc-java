package com.hedera.hcsapp.notifications;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

@Controller
public class WebAppNotifications {
    @MessageMapping("/notifications")
    @SendTo("/notifications/messages")
    public NotificationMessage send(NotificationMessage notification) throws Exception {
        NotificationMessage notificationMessage = new NotificationMessage();
        notificationMessage.setPayer(notification.getPayer());
        notificationMessage.setRecipient(notification.getRecipient());
        notificationMessage.setThreadId(notification.getThreadId());
        notificationMessage.setContext(notification.getContext());
        return notificationMessage;
    }
}