package site.dogether.notification.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import site.dogether.member.entity.Member;
import site.dogether.member.exception.MemberNotFoundException;
import site.dogether.member.repository.MemberRepository;
import site.dogether.notification.entity.NotificationToken;
import site.dogether.notification.exception.InvalidNotificationTokenException;
import site.dogether.notification.sender.firebase.SimpleFcmNotificationRequest;
import site.dogether.notification.repository.NotificationTokenRepository;
import site.dogether.notification.sender.NotificationSender;

@Slf4j
@Transactional(readOnly = true)
@RequiredArgsConstructor
@Service
public class NotificationService {

    private final NotificationTokenRepository notificationTokenRepository;
    private final NotificationSender notificationSender;
    private final MemberRepository memberRepository;

    @Transactional
    public void sendNotification(
        final Long recipientId,
        final String title,
        final String body,
        final String type
    ) {
        notificationTokenRepository.findAllByMember_Id(recipientId).forEach(
            notificationToken -> sendNotification(notificationToken, title, body, type));
    }

    private void sendNotification(final NotificationToken notificationToken, String title, String body, String type) {
        try {
            final SimpleFcmNotificationRequest simpleFcmNotificationRequest = new SimpleFcmNotificationRequest(notificationToken.getValue(), title, body, type);
            notificationSender.send(simpleFcmNotificationRequest);
        } catch (final InvalidNotificationTokenException e) {
            notificationTokenRepository.deleteAllByValue(notificationToken.getValue());
            log.info("유효하지 않은 토큰 제거 - {}", notificationToken.getValue());
        }
    }

    @Transactional
    public void saveNotificationToken(final Long memberId, final String notificationToken) {
        validateNotificationTokenIsNullOrEmpty(notificationToken);

        final Member member = getMember(memberId);
        if (notificationTokenRepository.existsByValue(notificationToken)) {
            log.trace("이미 존재하는 푸시 알림 토큰이므로 저장 X");
            return;
        }

        final NotificationToken notificationTokenJpaEntity = new NotificationToken(member, notificationToken);
        notificationTokenRepository.save(notificationTokenJpaEntity);
        log.trace("푸시 알림 토큰 저장 - {}", notificationToken);
    }

    private void validateNotificationTokenIsNullOrEmpty(final String token) {
        if (token == null || token.isEmpty()) {
            log.info("입력된 알림 토큰 값이 null 혹은 공백 - {}", token);
            throw new InvalidNotificationTokenException("알림 토큰 값은 공백일 수 없습니다.");
        }
    }

    private Member getMember(final Long memberId) {
        return memberRepository.findById(memberId)
                .orElseThrow(() -> new MemberNotFoundException(String.format("존재하지 않는 회원 id입니다. (%d)", memberId)));
    }

    @Transactional
    public void deleteNotificationToken(final Long memberId, final String notificationToken) {
        validateNotificationTokenIsNullOrEmpty(notificationToken);

        final Member member = getMember(memberId);
        notificationTokenRepository.findByMemberAndValue(member, notificationToken)
                .ifPresent(notificationTokenJpaEntity -> {
                    notificationTokenRepository.delete(notificationTokenJpaEntity);
                    log.info("푸시 알림 토큰 제거 - {}", notificationToken);});
    }
}
