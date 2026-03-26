package site.dogether.notification.config.redis;

import java.time.Duration;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.stream.Consumer;
import org.springframework.data.redis.connection.stream.MapRecord;
import org.springframework.data.redis.connection.stream.ReadOffset;
import org.springframework.data.redis.connection.stream.StreamOffset;
import org.springframework.data.redis.stream.StreamMessageListenerContainer;
import org.springframework.data.redis.stream.StreamMessageListenerContainer.StreamMessageListenerContainerOptions;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import site.dogether.notification.stream.NotificationStreamConstants;
import site.dogether.notification.stream.NotificationStreamConsumer;

@Slf4j
@RequiredArgsConstructor
@Configuration
public class RedisStreamConfig {

    private final RedisConnectionFactory redisConnectionFactory;

    @Bean
    public StreamMessageListenerContainer<String, MapRecord<String, String, String>> notificationStreamListenerContainer(
            final NotificationStreamConsumer consumer
    ) {
        final StreamMessageListenerContainerOptions<String, MapRecord<String, String, String>> options =
                StreamMessageListenerContainerOptions.builder()
                        .pollTimeout(Duration.ofSeconds(2))
                        .batchSize(10)
                        .executor(notificationStreamExecutor())
                        .build();

        final StreamMessageListenerContainer<String, MapRecord<String, String, String>> container =
                StreamMessageListenerContainer.create(redisConnectionFactory, options);

        initConsumerGroup();

        container.receive(
                Consumer.from(NotificationStreamConstants.CONSUMER_GROUP, generateConsumerName()),
                StreamOffset.create(NotificationStreamConstants.STREAM_KEY, ReadOffset.lastConsumed()),
                consumer
        );

        container.start();
        return container;
    }

    private void initConsumerGroup() {
        try {
            redisConnectionFactory.getConnection()
                    .streamCommands()
                    .xGroupCreate(
                            NotificationStreamConstants.STREAM_KEY.getBytes(),
                            NotificationStreamConstants.CONSUMER_GROUP,
                            ReadOffset.from("0"),
                            true
                    );
            log.info("Redis Stream consumer group 생성 완료 - {}", NotificationStreamConstants.CONSUMER_GROUP);
        } catch (final Exception e) {
            log.info("Consumer group 이미 존재하거나 생성 불가: {}", e.getMessage());
        }
    }

    @Bean
    public ThreadPoolTaskExecutor notificationStreamExecutor() {
        final ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(2);
        executor.setMaxPoolSize(4);
        executor.setQueueCapacity(50);
        executor.setThreadNamePrefix("notif-stream-");
        executor.initialize();
        return executor;
    }

    private String generateConsumerName() {
        return "consumer-" + UUID.randomUUID().toString().substring(0, 8);
    }
}
