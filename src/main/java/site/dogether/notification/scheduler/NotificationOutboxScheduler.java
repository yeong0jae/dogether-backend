package site.dogether.notification.scheduler;

import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import site.dogether.common.lock.NamedLockRepository;
import site.dogether.notification.entity.NotificationOutbox;
import site.dogether.notification.service.NotificationOutboxService;
import site.dogether.notification.service.NotificationService;

@Slf4j
@RequiredArgsConstructor
@Component
public class NotificationOutboxScheduler {

    private static final String LOCK_NAME = "NOTIFICATION_OUTBOX_PUBLISH";

    private final NotificationOutboxService notificationOutboxService;
    private final NotificationService notificationService;
    private final NamedLockRepository namedLockRepository;

    @Scheduled(fixedRate = 5000)
    public void publishPendingNotifications() {
        namedLockRepository.executeWithLock(LOCK_NAME, 0, this::processNotifications);
    }

    private void processNotifications() {
        final List<NotificationOutbox> pendingNotifications = notificationOutboxService.findPendingNotifications();

        for (final NotificationOutbox outbox : pendingNotifications) {
            try {
                notificationService.sendNotification(
                        outbox.getRecipientId(),
                        outbox.getTitle(),
                        outbox.getBody(),
                        outbox.getType()
                );
                notificationOutboxService.markAsSent(outbox.getId());
            } catch (final Exception e) {
                notificationOutboxService.markAsFailed(outbox.getId());
                log.error("알림 발송 실패 - outboxId: {}, recipientId: {}", outbox.getId(), outbox.getRecipientId(), e);
            }
        }
    }
}
