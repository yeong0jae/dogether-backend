package site.dogether.notification.batch;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import site.dogether.member.entity.Member;
import site.dogether.member.repository.MemberRepository;
import site.dogether.notification.service.NotificationService;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationBatchServiceTest {

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private NotificationBatchService notificationBatchService;

    @DisplayName("투두를 작성하지 않은 회원들에게 배치 알림을 전송한다")
    @Test
    void sendToMembersWhoNotWriteTodoToday() {
        // given
        final LocalDateTime startOfToday = LocalDateTime.of(LocalDate.now(), LocalTime.MIN);
        final LocalDateTime endOfToday = LocalDateTime.of(LocalDate.now(), LocalTime.MAX);

        final Member member1 = createMember(1L, "member1");
        final Member member2 = createMember(2L, "member2");
        final Member member3 = createMember(3L, "member3");

        when(memberRepository.findMembersWhoDidNotWriteTodoToday(startOfToday, endOfToday))
                .thenReturn(List.of(member1, member2, member3));

        // when
        notificationBatchService.sendToMembersWhoNotWriteTodoToday();

        // then
        final ArgumentCaptor<List<Long>> memberIdsCaptor = ArgumentCaptor.forClass(List.class);
        final ArgumentCaptor<String> titleCaptor = ArgumentCaptor.forClass(String.class);
        final ArgumentCaptor<String> bodyCaptor = ArgumentCaptor.forClass(String.class);
        final ArgumentCaptor<String> typeCaptor = ArgumentCaptor.forClass(String.class);

        verify(notificationService, times(1)).sendBatchNotification(
                memberIdsCaptor.capture(),
                titleCaptor.capture(),
                bodyCaptor.capture(),
                typeCaptor.capture()
        );

        final List<Long> capturedMemberIds = memberIdsCaptor.getValue();
        assertThat(capturedMemberIds).containsExactly(1L, 2L, 3L);
        assertThat(titleCaptor.getValue()).isEqualTo("투두 작성 알림");
        assertThat(bodyCaptor.getValue()).isEqualTo("오늘의 투두를 작성하지 않으셨네요! 지금 바로 작성해보세요!");
        assertThat(typeCaptor.getValue()).isEqualTo("REMINDER");
    }

    @DisplayName("투두를 작성하지 않은 회원이 없으면 알림을 전송하지 않는다")
    @Test
    void sendToMembersWhoNotWriteTodoToday_NoMembers() {
        // given
        final LocalDateTime startOfToday = LocalDateTime.of(LocalDate.now(), LocalTime.MIN);
        final LocalDateTime endOfToday = LocalDateTime.of(LocalDate.now(), LocalTime.MAX);

        when(memberRepository.findMembersWhoDidNotWriteTodoToday(startOfToday, endOfToday))
                .thenReturn(List.of());

        // when
        notificationBatchService.sendToMembersWhoNotWriteTodoToday();

        // then
        verify(notificationService, never()).sendBatchNotification(
                anyList(), anyString(), anyString(), anyString()
        );
    }

    @DisplayName("단일 회원에게도 배치 알림이 정상 동작한다")
    @Test
    void sendToMembersWhoNotWriteTodoToday_SingleMember() {
        // given
        final LocalDateTime startOfToday = LocalDateTime.of(LocalDate.now(), LocalTime.MIN);
        final LocalDateTime endOfToday = LocalDateTime.of(LocalDate.now(), LocalTime.MAX);

        final Member member = createMember(1L, "member1");

        when(memberRepository.findMembersWhoDidNotWriteTodoToday(startOfToday, endOfToday))
                .thenReturn(List.of(member));

        // when
        notificationBatchService.sendToMembersWhoNotWriteTodoToday();

        // then
        final ArgumentCaptor<List<Long>> memberIdsCaptor = ArgumentCaptor.forClass(List.class);

        verify(notificationService, times(1)).sendBatchNotification(
                memberIdsCaptor.capture(),
                anyString(),
                anyString(),
                anyString()
        );

        final List<Long> capturedMemberIds = memberIdsCaptor.getValue();
        assertThat(capturedMemberIds).containsExactly(1L);
    }

    @DisplayName("많은 수의 회원에게도 배치 알림이 정상 동작한다")
    @Test
    void sendToMembersWhoNotWriteTodoToday_ManyMembers() {
        // given
        final LocalDateTime startOfToday = LocalDateTime.of(LocalDate.now(), LocalTime.MIN);
        final LocalDateTime endOfToday = LocalDateTime.of(LocalDate.now(), LocalTime.MAX);

        final List<Member> members = List.of(
                createMember(1L, "member1"),
                createMember(2L, "member2"),
                createMember(3L, "member3"),
                createMember(4L, "member4"),
                createMember(5L, "member5"),
                createMember(6L, "member6"),
                createMember(7L, "member7"),
                createMember(8L, "member8"),
                createMember(9L, "member9"),
                createMember(10L, "member10")
        );

        when(memberRepository.findMembersWhoDidNotWriteTodoToday(startOfToday, endOfToday))
                .thenReturn(members);

        // when
        notificationBatchService.sendToMembersWhoNotWriteTodoToday();

        // then
        final ArgumentCaptor<List<Long>> memberIdsCaptor = ArgumentCaptor.forClass(List.class);

        verify(notificationService, times(1)).sendBatchNotification(
                memberIdsCaptor.capture(),
                anyString(),
                anyString(),
                anyString()
        );

        final List<Long> capturedMemberIds = memberIdsCaptor.getValue();
        assertThat(capturedMemberIds).hasSize(10);
        assertThat(capturedMemberIds).containsExactly(1L, 2L, 3L, 4L, 5L, 6L, 7L, 8L, 9L, 10L);
    }

    private Member createMember(final Long id, final String name) {
        return new Member(
                id,
                "provider_id_" + id,
                name,
                "profile_image_url",
                LocalDateTime.now()
        );
    }
}
