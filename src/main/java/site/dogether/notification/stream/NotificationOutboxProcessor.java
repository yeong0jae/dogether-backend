package site.dogether.notification.stream;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import site.dogether.notification.entity.NotificationOutbox;
import site.dogether.notification.entity.NotificationOutboxStatus;
import site.dogether.notification.repository.NotificationOutboxRepository;
import site.dogether.notification.service.NotificationService;

@Slf4j
@RequiredArgsConstructor
@Service
public class NotificationOutboxProcessor {

    private final NotificationOutboxRepository notificationOutboxRepository;
    private final NotificationService notificationService;

    public void process(final Long outboxId) {
        final NotificationOutbox outbox = findPendingOutbox(outboxId);
        if (outbox == null) {
            return;
        }

        try {
            notificationService.sendNotification(
                    outbox.getRecipientId(),
                    outbox.getTitle(),
                    outbox.getBody(),
                    outbox.getType()
            );
            markAsSent(outboxId);
        } catch (final Exception e) {
            markAsFailed(outboxId);
            log.error("알림 발송 실패 - outboxId: {}", outboxId, e);
        }
    }

    @Transactional(readOnly = true)
    public NotificationOutbox findPendingOutbox(final Long outboxId) {
        final NotificationOutbox outbox = notificationOutboxRepository.findById(outboxId)
                .orElse(null);

        if (outbox == null) {
            log.warn("존재하지 않는 outbox - outboxId: {}", outboxId);
            return null;
        }

        if (outbox.getStatus() != NotificationOutboxStatus.PENDING) {
            log.debug("이미 처리된 outbox - outboxId: {}, status: {}", outboxId, outbox.getStatus());
            return null;
        }

        return outbox;
    }

    @Transactional
    public void markAsSent(final Long outboxId) {
        notificationOutboxRepository.findById(outboxId)
                .ifPresent(NotificationOutbox::markAsSent);
    }

    @Transactional
    public void markAsFailed(final Long outboxId) {
        notificationOutboxRepository.findById(outboxId)
                .ifPresent(NotificationOutbox::markAsFailed);
    }
}
