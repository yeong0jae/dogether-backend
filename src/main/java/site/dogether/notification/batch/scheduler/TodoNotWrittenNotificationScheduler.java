package site.dogether.notification.batch.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import site.dogether.notification.batch.NotificationBatchService;

@Slf4j
@RequiredArgsConstructor
@Component
public class TodoNotWrittenNotificationScheduler {

    private final NotificationBatchService notificationBatchService;

    @Scheduled(cron = "0/20 * * * * *")
    @SchedulerLock(name = "TodoNotWrittenNotification", lockAtMostFor = "15s", lockAtLeastFor = "5s")
    public void scheduleNotificationToNotWriteTodoToday() {
        log.info("[스케줄러 실행] 매일 오전 9시 투두 미작성자 알림");

        try {
            notificationBatchService.sendToMembersWhoNotWriteTodoToday();
            log.info("[스케줄러 완료] 투두 미작성자 알림 발송 성공");
        } catch (Exception e) {
            log.error("[스케줄러 실패] 투두 미작성자 알림 발송 중 오류 발생", e);
        }
    }
}
