package com.digitopia.user.infrastructure.messaging;

import com.digitopia.common.dto.event.InvitationAcceptedEvent;
import com.digitopia.common.dto.event.OrganizationDeletedEvent;
import com.digitopia.user.domain.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class EventConsumer {

    private static final Logger log = LoggerFactory.getLogger(EventConsumer.class);
    private final UserService userService;

    public EventConsumer(UserService userService) {
        this.userService = userService;
    }


    @RabbitListener(queues = "invitation.accepted.queue")
    public void handleInvitationAccepted(InvitationAcceptedEvent event) {
        log.info("Received InvitationAcceptedEvent: userId={}, orgId={}",
            event.userId(), event.organizationId());

        try {
            userService.addUserToOrganization(event.userId(), event.organizationId());
            log.info("Successfully added user {} to organization {}",
                event.userId(), event.organizationId());
        } catch (Exception e) {
            log.error("Failed to add user {} to organization {}",
                event.userId(), event.organizationId(), e);
        }
    }


    @RabbitListener(queues = "organization.deleted.queue")
    public void handleOrganizationDeleted(OrganizationDeletedEvent event) {
        log.info("Received OrganizationDeletedEvent: orgId={}, deletedUserIds={}",
            event.organizationId(), event.deletedUserIds());

        try {
            userService.removeOrganizationFromUsers(
                event.organizationId(),
                event.deletedUserIds()
            );
            log.info("Successfully removed organization {} from {} users",
                event.organizationId(), event.deletedUserIds());
        } catch (Exception e) {
            log.error("Failed to remove organization {} from users",
                event.organizationId(), e);
        }
    }
}