package site.dogether.notification.sender.firebase;

import com.google.firebase.messaging.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import site.dogether.notification.exception.InvalidNotificationTokenException;
import site.dogether.notification.sender.NotificationRequest;
import site.dogether.notification.sender.NotificationSender;

import java.util.List;
import java.util.stream.IntStream;

@Slf4j
@Component
public class FcmNotificationSender implements NotificationSender {

    @Override
    public void send(final NotificationRequest request) {
        final FcmNotificationRequest fcmNotificationRequest = (FcmNotificationRequest) request;
        sendPushNotification(fcmNotificationRequest.convertFcmMessage());
    }

    public List<String> sendMulticast(final MulticastFcmNotificationRequest request) {
        final MulticastMessage multicastMessage = request.convertFcmMulticastMessage();
        return sendMulticastPushNotification(multicastMessage, request.fcmTokens());
    }

    private void sendPushNotification(final Message fcmMessage) {
        try {
            final String response = FirebaseMessaging.getInstance().send(fcmMessage);
            log.info("푸시 알림 전송 완료 - {}", response);
        } catch (final FirebaseMessagingException e) {
            log.error("푸시 알림 전송에 실패하였습니다.", e);
            handleFcmException(e.getMessage());
        }
    }

    private List<String> sendMulticastPushNotification(final MulticastMessage multicastMessage, final List<String> tokens) {
        try {
            final BatchResponse response = FirebaseMessaging.getInstance().sendEachForMulticast(multicastMessage);
            log.info("멀티캐스트 푸시 알림 전송 완료 - 성공: {}, 실패: {}",
                response.getSuccessCount(), response.getFailureCount());

            return extractInvalidTokens(response, tokens);
        } catch (final FirebaseMessagingException e) {
            log.error("멀티캐스트 푸시 알림 전송에 실패하였습니다.", e);
            throw new RuntimeException("멀티캐스트 푸시 알림 전송 실패", e);
        }
    }

    private List<String> extractInvalidTokens(final BatchResponse response, final List<String> tokens) {
        final List<SendResponse> responses = response.getResponses();
        return IntStream.range(0, responses.size())
            .filter(i -> !responses.get(i).isSuccessful())
            .filter(i -> responses.get(i).getException() != null)
            .filter(i -> checkInvalidFcmTokenResponse(responses.get(i).getException().getMessage()))
            .mapToObj(tokens::get)
            .toList();
    }

    private void handleFcmException(final String errorResponse) {
        if (checkInvalidFcmTokenResponse(errorResponse)) {
            throw new InvalidNotificationTokenException("유효하지 않은 FCM 토큰입니다.");
        }
    }

    private boolean checkInvalidFcmTokenResponse(final String errorResponse) {
        return errorResponse.contains("The registration token is not a valid FCM registration token");
    }
}
