package site.dogether.common.lock;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.sql.DataSource;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@RequiredArgsConstructor
@Component
public class NamedLockRepository {

    private final DataSource dataSource;

    public void executeWithLock(final String lockName, final int timeout, final Runnable action) {
        try (final Connection connection = dataSource.getConnection()) {
            try {
                getLock(connection, lockName, timeout);
                action.run();
            } finally {
                releaseLock(connection, lockName);
            }
        } catch (final SQLException e) {
            throw new RuntimeException("Named lock 처리 중 오류 발생", e);
        }
    }

    private void getLock(final Connection connection, final String lockName, final int timeout) throws SQLException {
        try (final PreparedStatement ps = connection.prepareStatement("SELECT GET_LOCK(?, ?)")) {
            ps.setString(1, lockName);
            ps.setInt(2, timeout);
            try (final ResultSet rs = ps.executeQuery()) {
                if (!rs.next() || rs.getInt(1) != 1) {
                    throw new SQLException("Named lock 획득 실패: " + lockName);
                }
            }
        }
    }

    private void releaseLock(final Connection connection, final String lockName) {
        try (final PreparedStatement ps = connection.prepareStatement("SELECT RELEASE_LOCK(?)")) {
            ps.setString(1, lockName);
            ps.executeQuery();
        } catch (final SQLException e) {
            log.error("Named lock 해제 실패: {}", lockName, e);
        }
    }
}
