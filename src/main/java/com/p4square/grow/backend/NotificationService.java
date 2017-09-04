package com.p4square.grow.backend;

/**
 * An implementation of NotificationService sends notifications.
 */
public interface NotificationService {

    /**
     * Send a notification from the GROW website to the notification address.
     *
     * @param message The notification to deliever.
     */
    void sendNotification(final String message);

}
