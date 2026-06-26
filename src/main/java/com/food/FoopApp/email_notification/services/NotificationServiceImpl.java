package com.food.FoopApp.email_notification.services;

import com.food.FoopApp.email_notification.domain.dto.NotificationDto;
import com.food.FoopApp.email_notification.domain.entity.Notification;
import com.food.FoopApp.email_notification.repository.NotificationRepository;
import com.food.FoopApp.enums.NotificationType;
import jakarta.mail.internet.MimeMessage;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;

@Service
@Slf4j
@AllArgsConstructor
public class NotificationServiceImpl implements NotificationService {
    private final JavaMailSender mailSender;
    private final NotificationRepository notificationRepository;

    @Override
    @Async
    public void sendEmail(NotificationDto notificationDto) {
        log.info("Inside sendEmail()");

        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper mimeMessageHelper = new MimeMessageHelper(
                    mimeMessage,
                    MimeMessageHelper.MULTIPART_MODE_MIXED_RELATED,
                    StandardCharsets.UTF_8.name()
            );

            mimeMessageHelper.setTo(notificationDto.getRecipient());
            mimeMessageHelper.setSubject(notificationDto.getSubject());
            mimeMessageHelper.setText(notificationDto.getBody(), notificationDto.isHtml());

            mailSender.send(mimeMessage);

            //Save to database
            Notification notification = Notification.builder()
                    .recipient(notificationDto.getRecipient())
                    .subject(notificationDto.getSubject())
                    .body(notificationDto.getBody())
                    .notificationType(NotificationType.EMAIL)
                    .isHtml(notificationDto.isHtml())
                    .build();

            notificationRepository.save(notification);
            log.info("Save notification to database");

        }catch (Exception e){
            throw new RuntimeException(e.getMessage());
        }
    }
}
