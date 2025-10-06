package com.digitopia.user.infrastructure.messaging;

import com.digitopia.common.dto.UserDTO;
import com.digitopia.common.dto.event.UserCreatedEvent;
import com.digitopia.common.dto.event.UserDeletedEvent;
import com.digitopia.user.infrastructure.config.RabbitMQConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Component
public class UserEventPublisher {

    private static final Logger log = LoggerFactory.getLogger(UserEventPublisher.class);
    private final RabbitTemplate rabbitTemplate;

    public UserEventPublisher(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void publishUserCreated(UserDTO user, UUID triggeredBy) {
        var event = new UserCreatedEvent(
            UUID.randomUUID(),
            LocalDateTime.now(),
            triggeredBy,
            user
        );

        try {
            rabbitTemplate.convertAndSend(
                RabbitMQConfig.USER_EXCHANGE,
                RabbitMQConfig.USER_CREATED_KEY,
                event
            );
        } catch (Exception e) {
            log.error("Failed to publish UserCreatedEvent for user: {}", user.email(), e);
        }

        log.info("Published UserCreatedEvent for user: {}", user.email());
    }

    public void publishUserDeleted(UUID userId, List<UUID> organizationIds, UUID triggeredBy) {
        var event = UserDeletedEvent.create(userId, organizationIds, triggeredBy);

        try {
            rabbitTemplate.convertAndSend(
                RabbitMQConfig.USER_EXCHANGE,
                RabbitMQConfig.USER_DELETED_KEY,
                event
            );
            log.info("Published UserDeletedEvent for user: {}, affecting {} organizations",
                userId, organizationIds.size());
        } catch (Exception e) {
            log.error("Failed to publish UserDeletedEvent for user: {}", userId, e);
        }
    }
}
