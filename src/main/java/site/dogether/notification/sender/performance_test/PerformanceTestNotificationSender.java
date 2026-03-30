package site.dogether.notification.sender.performance_test;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import site.dogether.notification.sender.NotificationRequest;
import site.dogether.notification.sender.NotificationSender;

@Slf4j
@Profile("pt")
@Primary
@Component
public class PerformanceTestNotificationSender implements NotificationSender {

    @Override
    public void send(final NotificationRequest request) {
        try {
            Thread.sleep(150); // 실제 알림 전송처럼 대기
            log.info("performance test 푸시 알림 전송");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt(); // 중단 플래그 복구
            log.warn("알림 전송 중 인터럽트 발생!", e);
        }
    }
}
