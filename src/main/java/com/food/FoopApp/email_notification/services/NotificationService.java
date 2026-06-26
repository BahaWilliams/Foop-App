package com.food.FoopApp.email_notification.services;


import com.food.FoopApp.email_notification.domain.dto.NotificationDto;

public interface NotificationService {
    public void sendEmail(NotificationDto notificationDto);
}
