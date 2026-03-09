package site.dogether.notification.scheduler;

import java.time.Duration;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import site.dogether.common.lock.RedisDistributedLockProvider;
import site.dogether.notification.entity.NotificationOutbox;
import site.dogether.notification.service.NotificationOutboxService;
import site.dogether.notification.service.NotificationService;

@Slf4j
@RequiredArgsConstructor
@Component
public class NotificationOutboxScheduler {

    private static final String LOCK_KEY = "notification-outbox-publish-lock";
    private static final Duration LOCK_TTL = Duration.ofSeconds(5);

    private final NotificationOutboxService notificationOutboxService;
    private final NotificationService notificationService;
    private final RedisDistributedLockProvider lockProvider;

    @Scheduled(fixedRate = 5000)
    public void publishPendingNotifications() {
        final String lockValue = UUID.randomUUID().toString();

        if (!Boolean.TRUE.equals(lockProvider.lock(LOCK_KEY, lockValue, LOCK_TTL))) {
            return;
        }

        try {
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
                    log.error("알림 발송 실패 - outboxId: {}, recipientId: {}",
                            outbox.getId(), outbox.getRecipientId(), e);
                }
            }
        } finally {
            lockProvider.unlock(LOCK_KEY, lockValue);
        }
    }
}
