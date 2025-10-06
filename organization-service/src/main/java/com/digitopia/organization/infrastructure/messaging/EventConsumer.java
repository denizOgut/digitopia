package com.digitopia.organization.infrastructure.messaging;

import com.digitopia.common.dto.event.InvitationAcceptedEvent;
import com.digitopia.common.dto.event.UserDeletedEvent;
import com.digitopia.organization.domain.service.OrganizationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class EventConsumer {

    private static final Logger log = LoggerFactory.getLogger(EventConsumer.class);
    private final OrganizationService organizationService;

    public EventConsumer(OrganizationService organizationService) {
        this.organizationService = organizationService;
    }


    @RabbitListener(queues = "invitation.accepted.queue")
    public void handleInvitationAccepted(InvitationAcceptedEvent event) {
        log.info("Received InvitationAcceptedEvent: userId={}, orgId={}",
            event.userId(), event.organizationId());

        try {
            organizationService.addUserToOrganization(
                event.organizationId(),
                event.userId()
            );
            log.info("Successfully added user {} to organization {}",
                event.userId(), event.organizationId());
        } catch (Exception e) {
            log.error("Failed to add user {} to organization {}",
                event.userId(), event.organizationId(), e);
        }
    }
}
