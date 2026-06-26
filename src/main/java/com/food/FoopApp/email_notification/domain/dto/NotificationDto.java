package com.food.FoopApp.email_notification.domain.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.food.FoopApp.enums.NotificationType;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
@Builder
public class NotificationDto {
    private Long id;
    private String subject;

    @NotBlank(message = "recipient must be required")
    private String recipient;

    private String body;
    private NotificationType notificationType;
    private LocalDateTime createdAt;
    private boolean isHtml;
}
