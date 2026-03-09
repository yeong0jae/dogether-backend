package site.dogether.notification.scheduler;

import java.util.List;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import site.dogether.notification.entity.NotificationOutbox;
import site.dogether.notification.service.NotificationOutboxService;
import site.dogether.notification.service.NotificationService;

@Slf4j
@RequiredArgsConstructor
@Component
public class NotificationOutboxScheduler {

    private static final String LOCK_KEY = "notification-outbox-publish-lock";

    private final NotificationOutboxService notificationOutboxService;
    private final NotificationService notificationService;
    private final RedissonClient redissonClient;

    @Scheduled(fixedRate = 5000)
    public void publishPendingNotifications() {
        final RLock lock = redissonClient.getLock(LOCK_KEY);

        if (!lock.tryLock()) {
            return;
        }

        try {
            final List<NotificationOutbox> pendingNotifications =
                    notificationOutboxService.findPendingNotifications();

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
            lock.unlock();
        }
    }
}
