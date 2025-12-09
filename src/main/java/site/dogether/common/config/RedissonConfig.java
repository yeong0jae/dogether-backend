package site.dogether.common.config;

import lombok.extern.slf4j.Slf4j;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

@Slf4j
@Configuration
public class RedissonConfig {

    @Value("${spring.data.redis.host}")
    private String redisHost;

    @Value("${spring.data.redis.port}")
    private int redisPort;

    @Bean
    public RedissonClient redissonClient() {
        String redisAddress = "redis://" + redisHost + ":" + redisPort;

        Config config = new Config();
        config.setLockWatchdogTimeout(30000)
                .useSingleServer()
                .setAddress(redisAddress);

        RedissonClient redissonClient = Redisson.create(config);
        log.info("RedissonClient 생성 완료");

        return redissonClient;
    }
}
