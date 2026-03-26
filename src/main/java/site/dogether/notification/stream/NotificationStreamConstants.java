package site.dogether.notification.stream;

public final class NotificationStreamConstants {

    public static final String STREAM_KEY = "notification:outbox:stream";
    public static final String CONSUMER_GROUP = "notification-consumers";
    public static final String LOCK_PREFIX = "notification:outbox:lock:";

    private NotificationStreamConstants() {
    }
}
