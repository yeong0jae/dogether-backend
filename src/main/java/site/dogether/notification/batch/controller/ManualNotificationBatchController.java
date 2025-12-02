package site.dogether.notification.batch.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import site.dogether.notification.batch.NotificationBatchService;

@RestController
@RequiredArgsConstructor
public class ManualNotificationBatchController {

    private final NotificationBatchService notificationBatchService;

    @PostMapping("/api/notification-batch/trigger-not-write-todo-today")
    public void triggerNotificationNotWriteTodoToday() {
        notificationBatchService.sendToMembersWhoNotWriteTodoToday();
    }
}
