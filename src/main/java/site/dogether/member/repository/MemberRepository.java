package site.dogether.member.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import site.dogether.member.entity.Member;

public interface MemberRepository extends JpaRepository<Member, Long> {

    Optional<Member> findByProviderId(String providerId);

    @Query("SELECT m FROM Member m WHERE NOT EXISTS (" +
            "    SELECT dt FROM DailyTodo dt " +
            "    WHERE dt.member = m " +
            "    AND dt.writtenAt BETWEEN :startOfToday AND :endOfToday" +
            ")")
    List<Member> findMembersWhoDidNotWriteTodoToday(
            @Param("startOfToday") LocalDateTime startOfToday,
            @Param("endOfToday") LocalDateTime endOfToday
    );
}
