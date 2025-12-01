package site.dogether.notification.sender.firebase;

import com.google.firebase.messaging.*;
import site.dogether.notification.sender.NotificationRequest;

import java.util.List;

public record MulticastFcmNotificationRequest(
        List<String> fcmTokens,
        String title,
        String body,
        String type
) implements NotificationRequest {

    public MulticastMessage convertFcmMulticastMessage() {
        return MulticastMessage.builder()
                .setNotification(createNotification())
                .addAllTokens(fcmTokens)
                .setApnsConfig(createApnsConfig())
                .setAndroidConfig(createAndroidConfig())
                .putData("type", type)
                .build();
    }

    private Notification createNotification() {
        return Notification.builder()
                .setTitle(title)
                .setBody(body)
                .build();
    }

    private ApnsConfig createApnsConfig() {
        return ApnsConfig.builder()
                .setAps(Aps.builder().setContentAvailable(true).build())
                .build();
    }

    private static AndroidConfig createAndroidConfig() {
        return AndroidConfig.builder()
                .setPriority(AndroidConfig.Priority.HIGH)
                .build();
    }
}
