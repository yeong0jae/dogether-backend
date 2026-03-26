package site.dogether.notification.stream;

import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.data.redis.connection.stream.MapRecord;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.stream.StreamListener;
import org.springframework.stereotype.Component;

@Slf4j
@RequiredArgsConstructor
@Component
public class NotificationStreamConsumer implements StreamListener<String, MapRecord<String, String, String>> {

    private static final long LOCK_WAIT_MS = 0;
    private static final long LOCK_LEASE_MS = 30_000;

    private final RedissonClient redissonClient;
    private final NotificationOutboxProcessor notificationOutboxProcessor;
    private final StringRedisTemplate redisTemplate;

    @Override
    public void onMessage(final MapRecord<String, String, String> message) {
        final Long outboxId = Long.valueOf(message.getValue().get("outboxId"));
        final RLock lock = redissonClient.getLock(NotificationStreamConstants.LOCK_PREFIX + outboxId);

        try {
            final boolean acquired = lock.tryLock(LOCK_WAIT_MS, LOCK_LEASE_MS, TimeUnit.MILLISECONDS);
            if (!acquired) {
                log.debug("RLock 획득 실패, 다른 consumer가 처리 중 - outboxId: {}", outboxId);
                acknowledge(message);
                return;
            }

            try {
                notificationOutboxProcessor.process(outboxId);
            } finally {
                if (lock.isHeldByCurrentThread()) {
                    lock.unlock();
                }
            }

            acknowledge(message);
        } catch (final InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("인터럽트 발생 - outboxId: {}", outboxId, e);
        } catch (final Exception e) {
            log.error("메시지 처리 실패 - outboxId: {}", outboxId, e);
            acknowledge(message);
        }
    }

    private void acknowledge(final MapRecord<String, String, String> message) {
        redisTemplate.opsForStream().acknowledge(NotificationStreamConstants.CONSUMER_GROUP, message);
    }
}
