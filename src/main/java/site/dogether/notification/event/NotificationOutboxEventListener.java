package site.dogether.notification.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import site.dogether.notification.stream.NotificationStreamPublisher;

@Slf4j
@RequiredArgsConstructor
@Component
public class NotificationOutboxEventListener {

    private final NotificationStreamPublisher notificationStreamPublisher;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleOutboxCreated(final NotificationOutboxEvent event) {
        try {
            notificationStreamPublisher.publish(event.outboxId());
        } catch (final Exception e) {
            log.warn("Redis Stream 발행 실패 - outboxId: {}. 폴링 스케줄러가 재발행합니다.", event.outboxId(), e);
        }
    }
}
