package site.dogether.notification.batch.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import site.dogether.notification.batch.NotificationBatchService;

import java.util.concurrent.TimeUnit;

@Slf4j
@RequiredArgsConstructor
@Component
public class TodoNotWrittenNotificationScheduler {

    private static final String LOCK_KEY = "TodoNotWrittenNotification";
    private static final long WAIT_TIME = 1L;

    private final NotificationBatchService notificationBatchService;
    private final RedissonClient redissonClient;

    @Scheduled(cron = "0/20 * * * * *")
    public void scheduleNotificationToNotWriteTodoToday() {
        final RLock lock = redissonClient.getLock(LOCK_KEY);

        try {
            final boolean isLocked = lock.tryLock(WAIT_TIME, TimeUnit.SECONDS);

            if (!isLocked) {
                log.info("[스케줄러 스킵] 다른 인스턴스에서 실행 중");
                return;
            }

            log.info("[스케줄러 실행] 매일 오전 9시 투두 미작성자 알림");

            try {
                notificationBatchService.sendToMembersWhoNotWriteTodoToday();
                log.info("[스케줄러 완료] 투두 미작성자 알림 발송 성공");
            } catch (Exception e) {
                log.error("[스케줄러 실패] 투두 미작성자 알림 발송 중 오류 발생", e);
            }
        } catch (InterruptedException e) {
            log.error("[스케줄러 인터럽트] 락 획득 중 인터럽트 발생", e);
            Thread.currentThread().interrupt();
        } finally {
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }
}
