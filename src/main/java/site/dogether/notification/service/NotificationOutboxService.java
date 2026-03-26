package site.dogether.notification.service;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import site.dogether.notification.entity.NotificationOutbox;
import site.dogether.notification.entity.NotificationOutboxStatus;
import site.dogether.notification.event.NotificationOutboxEvent;
import site.dogether.notification.repository.NotificationOutboxRepository;

@RequiredArgsConstructor
@Service
public class NotificationOutboxService {

    private final NotificationOutboxRepository notificationOutboxRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public void save(final Long recipientId, final String title, final String body, final String type) {
        final NotificationOutbox outbox = new NotificationOutbox(recipientId, title, body, type);
        notificationOutboxRepository.save(outbox);
        eventPublisher.publishEvent(new NotificationOutboxEvent(outbox.getId()));
    }

    @Transactional(readOnly = true)
    public List<NotificationOutbox> findPendingNotifications() {
        return notificationOutboxRepository.findAllByStatus(NotificationOutboxStatus.PENDING);
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
