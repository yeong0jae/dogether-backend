package site.dogether.notification.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import site.dogether.notification.entity.NotificationOutbox;
import site.dogether.notification.entity.NotificationOutboxStatus;

public interface NotificationOutboxRepository extends JpaRepository<NotificationOutbox, Long> {

    List<NotificationOutbox> findAllByStatus(NotificationOutboxStatus status);
}
