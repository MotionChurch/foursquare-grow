package com.p4square.grow.provider;

import com.p4square.grow.backend.NotificationService;

/**
 * Indicates that the class can provide a NotificationService instance.
 */
public interface ProvidesNotificationService {

    NotificationService getNotificationService();
}
