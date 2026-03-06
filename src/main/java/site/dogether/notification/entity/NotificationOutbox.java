package site.dogether.notification.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import site.dogether.common.audit.entity.BaseEntity;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "notification_outbox")
public class NotificationOutbox extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "recipient_id", nullable = false)
    private Long recipientId;

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "body", nullable = false)
    private String body;

    @Column(name = "type", nullable = false)
    private String type;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private NotificationOutboxStatus status;

    public NotificationOutbox(final Long recipientId, final String title, final String body, final String type) {
        this.recipientId = recipientId;
        this.title = title;
        this.body = body;
        this.type = type;
        this.status = NotificationOutboxStatus.PENDING;
    }

    public void markAsSent() {
        this.status = NotificationOutboxStatus.SENT;
    }

    public void markAsFailed() {
        this.status = NotificationOutboxStatus.FAILED;
    }
}
