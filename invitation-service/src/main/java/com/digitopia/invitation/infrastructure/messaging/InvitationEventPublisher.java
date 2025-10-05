package com.digitopia.invitation.infrastructure.messaging;

import com.digitopia.common.dto.event.InvitationAcceptedEvent;
import com.digitopia.common.dto.event.InvitationExpiredEvent;
import com.digitopia.invitation.infrastructure.config.RabbitMQConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Component
public class InvitationEventPublisher {

    private static final Logger log = LoggerFactory.getLogger(InvitationEventPublisher.class);
    private final RabbitTemplate rabbitTemplate;

    public InvitationEventPublisher(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void publishInvitationAccepted(UUID userId, UUID organizationId, UUID invitationId, UUID triggeredBy) {
        var event = new InvitationAcceptedEvent(
            UUID.randomUUID(),
            LocalDateTime.now(),
            triggeredBy,
            userId,
            organizationId,
            invitationId
        );

        try{
            rabbitTemplate.convertAndSend(
                RabbitMQConfig.INVITATION_EXCHANGE,
                RabbitMQConfig.INVITATION_ACCEPTED_KEY,
                event
            );

            log.info("Published InvitationAcceptedEvent for user: {} to org: {}", userId, organizationId);
        } catch (Exception e) {
            log.error("Error publishing InvitationAcceptedEvent for user: {} to org: {}", userId, organizationId);
        }

    }

    public void publishInvitationsExpired(List<UUID> invitationIds, UUID triggeredBy) {
        var event = new InvitationExpiredEvent(
            UUID.randomUUID(),
            LocalDateTime.now(),
            triggeredBy,
            invitationIds
        );

        try{
            rabbitTemplate.convertAndSend(
                RabbitMQConfig.INVITATION_EXCHANGE,
                RabbitMQConfig.INVITATION_EXPIRED_KEY,
                event
            );

            log.info("Published InvitationExpiredEvent for {} invitations", invitationIds.size());
        } catch (Exception e) {
            log.error("Error publishing InvitationExpiredEvent for {} invitations", invitationIds.size());
        }
    }
}
