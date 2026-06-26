package com.food.FoopApp;

import com.food.FoopApp.email_notification.domain.dto.NotificationDto;
import com.food.FoopApp.email_notification.services.NotificationService;
import com.food.FoopApp.enums.NotificationType;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
//@RequiredArgsConstructor
public class FoopAppApplication {
//    private final NotificationService notificationService;

	public static void main(String[] args) {
		SpringApplication.run(FoopAppApplication.class, args);
	}

//    @Bean
//    public CommandLineRunner runner(){
//        return (args) -> {
//            NotificationDto notificationDto = NotificationDto.builder()
//                    .recipient("bahawilliamsazael@gmail.com")
//                    .subject("Hello will")
//                    .body("Test email")
//                    .notificationType(NotificationType.EMAIL)
//                    .build();
//
//            notificationService.sendEmail(notificationDto);
//        };
//    }
}
