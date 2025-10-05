package com.digitopia.invitation;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication(scanBasePackages = {
    "com.digitopia.invitation",
    "com.digitopia.common"
})
@EnableDiscoveryClient
@EnableJpaAuditing
@EnableScheduling
public class InvitationServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(InvitationServiceApplication.class, args);
    }
}
