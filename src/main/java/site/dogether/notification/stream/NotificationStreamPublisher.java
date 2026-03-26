package site.dogether.notification.stream;

import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.stream.RecordId;
import org.springframework.data.redis.connection.stream.StreamRecords;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@RequiredArgsConstructor
@Component
public class NotificationStreamPublisher {

    private final StringRedisTemplate redisTemplate;

    public void publish(final Long outboxId) {
        final RecordId recordId = redisTemplate.opsForStream()
                .add(StreamRecords.newRecord()
                        .in(NotificationStreamConstants.STREAM_KEY)
                        .ofMap(Map.of("outboxId", String.valueOf(outboxId))));
        log.debug("Redis Stream 발행 - outboxId: {}, recordId: {}", outboxId, recordId);
    }
}
