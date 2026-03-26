package site.dogether.notification.scheduler;

import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import site.dogether.notification.entity.NotificationOutbox;
import site.dogether.notification.repository.NotificationOutboxRepository;
import site.dogether.notification.stream.NotificationStreamPublisher;

@Slf4j
@RequiredArgsConstructor
@Component
public class NotificationOutboxScheduler {

    private final NotificationOutboxRepository notificationOutboxRepository;
    private final NotificationStreamPublisher notificationStreamPublisher;

    @Scheduled(fixedRate = 300_000)
    public void publishStalePendingNotifications() {
        final LocalDateTime threshold = LocalDateTime.now().minusMinutes(5);
        final List<NotificationOutbox> staleNotifications = notificationOutboxRepository.findAllPendingOlderThan(threshold);

        for (final NotificationOutbox outbox : staleNotifications) {
            try {
                notificationStreamPublisher.publish(outbox.getId());
                log.info("폴링 재발행 - outboxId: {}", outbox.getId());
            } catch (final Exception e) {
                log.error("폴링 재발행 실패 - outboxId: {}", outbox.getId(), e);
            }
        }
    }
}
