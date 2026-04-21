package com.maayn.notificationservice.services.notification;

import maayn.veld.generated.errors.*;
import maayn.veld.generated.models.notification.*;
import maayn.veld.generated.services.INotificationService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class NotificationServiceImpl implements INotificationService {
    @Override
    public List<NotificationItem> listNotifications() throws Exception {
        return List.of();
    }

    @Override
    public NotificationItem getNotification(String id) throws GetNotificationException, Exception {
        return null;
    }

    @Override
    public NotificationItem sendNotification(SendNotificationInput input) throws SendNotificationException, Exception {
        return null;
    }

    @Override
    public NotificationItem retryNotification(String id) throws RetryNotificationException, Exception {
        return null;
    }

    @Override
    public List<NotificationTemplate> listTemplates() throws Exception {
        return List.of();
    }

    @Override
    public NotificationTemplate getTemplate(String id) throws GetTemplateException, Exception {
        return null;
    }

    @Override
    public NotificationTemplate createTemplate(CreateTemplateInput input) throws CreateTemplateException, Exception {
        return null;
    }

    @Override
    public NotificationTemplate updateTemplate(String id, UpdateTemplateInput input) throws UpdateTemplateException, Exception {
        return null;
    }

    @Override
    public NotificationTemplate deactivateTemplate(String id) throws DeactivateTemplateException, Exception {
        return null;
    }
}
