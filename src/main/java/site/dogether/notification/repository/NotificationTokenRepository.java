package site.dogether.notification.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import site.dogether.member.entity.Member;
import site.dogether.notification.entity.NotificationToken;

import java.util.List;
import java.util.Optional;

public interface NotificationTokenRepository extends JpaRepository<NotificationToken, Long> {

    boolean existsByValue(String token);

    List<NotificationToken> findAllByMember_Id(Long memberId);

    void deleteAllByValue(String value);

    Optional<NotificationToken> findByMemberAndValue(Member member, String value);

    @Query("SELECT nt.value FROM NotificationToken nt WHERE nt.member.id IN :memberIds")
    List<String> findTokenValuesByMemberIds(@Param("memberIds") List<Long> memberIds);
}
