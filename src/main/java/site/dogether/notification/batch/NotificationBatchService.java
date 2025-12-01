package site.dogether.notification.batch;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import site.dogether.challengegroup.repository.ChallengeGroupMemberRepository;
import site.dogether.dailytodo.repository.DailyTodoRepository;
import site.dogether.member.entity.Member;
import site.dogether.member.repository.MemberRepository;
import site.dogether.notification.service.NotificationService;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Collections;
import java.util.List;

@Slf4j
@Transactional(readOnly = true)
@RequiredArgsConstructor
@Service
public class NotificationBatchService {

    private final NotificationService notificationService;
    private final MemberRepository memberRepository;
    
    public void sendToMembersWhoNotWriteTodoToday() {
        log.info("[배치 시작] 투두 미작성자 조회");

        final LocalDateTime startOfToday = LocalDateTime.of(LocalDate.now(), LocalTime.MIN);
        final LocalDateTime endOfToday = LocalDateTime.of(LocalDate.now(), LocalTime.MAX);

        final List<Member> membersWhoDidNotWriteTodoToday = memberRepository.findMembersWhoDidNotWriteTodoToday(
                startOfToday, endOfToday
        );

        final List<Long> memberIds = membersWhoDidNotWriteTodoToday.stream()
                .map(Member::getId)
                .toList();

        notificationService.sendBatchNotification(
                memberIds,
                "투두 작성 알림",
                "오늘의 투두를 작성하지 않으셨네요! 지금 바로 작성해보세요!",
                "REMINDER"
        );
    }
}
