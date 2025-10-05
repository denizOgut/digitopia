package com.digitopia.invitation.infrastructure.scheduler;

import com.digitopia.invitation.domain.service.InvitationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Scheduled job that expires pending invitations older than 7 days.
 * Runs daily at midnight (00:00).
 * Updates invitation status from PENDING to EXPIRED and publishes event.
 *
 */
@Component
public class InvitationExpiryScheduler {

    private static final Logger log = LoggerFactory.getLogger(InvitationExpiryScheduler.class);
    private final InvitationService invitationService;

    public InvitationExpiryScheduler(InvitationService invitationService) {
        this.invitationService = invitationService;
    }

    /**
     * Executes invitation expiry job.
     * Called automatically by Spring @Scheduled at midnight.
     */
    @Scheduled(cron = "@midnight")
    public void expireOldInvitations() {
        log.info("Starting invitation expiry job...");

        try {
            invitationService.expireOldInvitations();
            log.info("Invitation expiry job completed successfully");
        } catch (Exception e) {
            log.error("Error during invitation expiry job", e);
        }
    }
}
