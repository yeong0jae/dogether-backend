package site.dogether.notification.service;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import site.dogether.notification.entity.NotificationOutbox;
import site.dogether.notification.entity.NotificationOutboxStatus;
import site.dogether.notification.repository.NotificationOutboxRepository;

@RequiredArgsConstructor
@Service
public class NotificationOutboxService {

    private final NotificationOutboxRepository notificationOutboxRepository;

    @Transactional
    public void save(final Long recipientId, final String title, final String body, final String type) {
        final NotificationOutbox outbox = new NotificationOutbox(recipientId, title, body, type);
        notificationOutboxRepository.save(outbox);
    }

    @Transactional
    public List<NotificationOutbox> findAndMarkAsProcessing() {
        final List<NotificationOutbox> pendingNotifications = notificationOutboxRepository.findAllByStatus(NotificationOutboxStatus.PENDING);
        pendingNotifications.forEach(NotificationOutbox::markAsProcessing);
        return pendingNotifications;
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
