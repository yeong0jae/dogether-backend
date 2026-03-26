package site.dogether.notification.repository;

import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import site.dogether.notification.entity.NotificationOutbox;
import site.dogether.notification.entity.NotificationOutboxStatus;

public interface NotificationOutboxRepository extends JpaRepository<NotificationOutbox, Long> {

    List<NotificationOutbox> findAllByStatus(NotificationOutboxStatus status);

    @Query("SELECT o FROM NotificationOutbox o WHERE o.status = 'PENDING' AND o.rowInsertedAt < :threshold")
    List<NotificationOutbox> findAllPendingOlderThan(@Param("threshold") LocalDateTime threshold);
}
