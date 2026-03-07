package site.dogether.notification.repository;

import java.util.List;
import jakarta.persistence.LockModeType;
import jakarta.persistence.QueryHint;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.QueryHints;
import site.dogether.notification.entity.NotificationOutbox;
import site.dogether.notification.entity.NotificationOutboxStatus;

public interface NotificationOutboxRepository extends JpaRepository<NotificationOutbox, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @QueryHints({@QueryHint(name = "jakarta.persistence.lock.timeout", value = "-2")})
    List<NotificationOutbox> findAllByStatus(NotificationOutboxStatus status);
}
