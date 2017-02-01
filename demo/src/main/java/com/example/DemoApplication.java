package com.example;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;

import java.util.Arrays;
import java.util.UUID;

@SpringBootApplication
public class DemoApplication {

    public static void main(String[] args) {
        Long number = Long.parseLong(args[0]);
        ApplicationContext applicationContext = SpringApplication.run(DemoApplication.class, args);
        NotificationService notificationService = applicationContext.getBean(NotificationService.class);
        for (int i = 0; i < number; i++) {
            notificationService.notifySystem();
        }
    }
}
