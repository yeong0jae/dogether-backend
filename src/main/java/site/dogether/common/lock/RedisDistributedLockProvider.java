package site.dogether.common.lock;

import java.time.Duration;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class RedisDistributedLockProvider {

    private static final DefaultRedisScript<Long> UNLOCK_SCRIPT = new DefaultRedisScript<>(
            "if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end",
            Long.class
    );

    private final StringRedisTemplate redisTemplate;

    public Boolean lock(final String key, final String lockValue, final Duration ttl) {
        return redisTemplate.opsForValue()
                .setIfAbsent(key, lockValue, ttl);
    }

    public void unlock(final String key, final String lockValue) {
        redisTemplate.execute(UNLOCK_SCRIPT, List.of(key), lockValue);
    }
}
