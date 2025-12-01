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

    /**
     * 오늘 투두를 작성하지 않은 회원에게 알림 발송
     */
    public void sendToMembersWhoNotWriteTodoToday() {
        log.info("[배치 시작] 투두 미작성자 조회");

        final LocalDateTime startOfToday = LocalDateTime.of(LocalDate.now(), LocalTime.MIN);
        final LocalDateTime endOfToday = LocalDateTime.of(LocalDate.now(), LocalTime.MAX);

        // 오늘 생성된 투두가 존재하는 회원 조회하기
        final List<Member> membersWhoDidNotWriteTodoToday = memberRepository.findMembersWhoDidNotWriteTodoToday(
                startOfToday, endOfToday
        );

        // 해당 회원들에게 알림 발송하기
        for (final Member member : membersWhoDidNotWriteTodoToday) {
            notificationService.sendNotification(
                    member.getId(),
                    "투두 작성 알림",
                    "오늘의 투두를 작성하지 않으셨네요! 지금 바로 작성해보세요!",
                    "REMINDER"
            );
            log.info("[알림 발송] 회원 ID: {} - 오늘의 투두 작성 알림 발송 완료", member.getId());
        }
    }

    /**
     * 2. 투두 작성 후 6시간 동안 인증하지 않은 회원에게 알림 발송
     * TODO: 구현 예정
     */
    public void sendNotificationToMembersWhoDidNotCertifyTodo() {
        log.info("[배치 시작] 투두 미인증자 조회");
    }

    /**
     * 3. 검사자로 선정된 후 3시간 동안 검사하지 않은 회원에게 알림 발송
     * TODO: 구현 예정
     */
    public void sendNotificationToReviewersWhoDidNotReview() {
        log.info("🔍 [배치 시작] 검사 미완료자 조회");
    }
}
