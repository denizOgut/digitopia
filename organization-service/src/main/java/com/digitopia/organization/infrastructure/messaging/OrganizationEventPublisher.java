package com.digitopia.organization.infrastructure.messaging;

import com.digitopia.common.dto.OrganizationDTO;
import com.digitopia.common.dto.event.OrganizationCreatedEvent;
import com.digitopia.organization.infrastructure.config.RabbitMQConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.UUID;

@Component
public class OrganizationEventPublisher {

    private static final Logger log = LoggerFactory.getLogger(OrganizationEventPublisher.class);
    private final RabbitTemplate rabbitTemplate;

    public OrganizationEventPublisher(RabbitTemplate rabbitTemplate)
    {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void publishOrganizationCreated(OrganizationDTO org, UUID triggeredBy)
    {
        var event = new OrganizationCreatedEvent(
            UUID.randomUUID(),
            LocalDateTime.now(),
            triggeredBy,
            org
        );

        try {
            rabbitTemplate.convertAndSend(
                RabbitMQConfig.ORG_EXCHANGE,
                RabbitMQConfig.ORG_CREATED_KEY,
                event);
            log.info("Published OrganizationCreatedEvent for: {}", org.organizationName());

        } catch (Exception e) {
            log.error("Failed to publish OrganizationCreatedEvent for: {}", org.organizationName(), e);
        }
    }
}

