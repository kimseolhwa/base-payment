package com.github.shkim.base.common.config;

import io.github.bucket4j.distributed.proxy.ProxyManager;
import io.github.bucket4j.redis.lettuce.cas.LettuceBasedProxyManager;
import io.lettuce.core.RedisClient;
import io.lettuce.core.RedisURI;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.codec.ByteArrayCodec;
import io.lettuce.core.codec.RedisCodec;
import io.lettuce.core.codec.StringCodec;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;

/**
 * Redis 연동을 위한 전역 설정 클래스.
 * <p>
 * 멱등성 검증, 글로벌 세션 등 문자열 기반의 빠른 데이터 처리를 위해
 * StringRedisTemplate 빈(Bean) 등록 수행
 * </p>
 */
@Configuration
public class RedisConfig {

    @Value("${spring.data.redis.host:localhost}")
    private String redisHost;

    @Value("${spring.data.redis.port:6379}")
    private int redisPort;

    /**
     * 문자열 전용 Redis 연산 템플릿 생성 및 반환.
     *
     * @param connectionFactory 스프링 부트가 자동 구성한 Redis 연결 팩토리
     * @return 설정이 완료된 StringRedisTemplate 객체
     */
    @Bean
    public StringRedisTemplate stringRedisTemplate(RedisConnectionFactory connectionFactory) {
        return new StringRedisTemplate(connectionFactory);
    }

    /**
     * Bucket4j Redis 지원을 위한 Lettuce 클라이언트 빈 생성.
     */
    @Bean
    public RedisClient redisClient() {
        return RedisClient.create(RedisURI.builder()
                .withHost(redisHost)
                .withPort(redisPort)
                .build());
    }

    /**
     * Bucket4j 분산 환경(Redis) 처리를 위한 ProxyManager 빈 생성.
     */
    @Bean
    public ProxyManager<String> bucket4jProxyManager(RedisClient redisClient) {
        StatefulRedisConnection<String, byte[]> connection = redisClient.connect(
                RedisCodec.of(StringCodec.UTF8, ByteArrayCodec.INSTANCE)
        );

        // Bucket4j 8.x 이상에서는 withExpirationStrategy() 대신
        // 기본적으로 BucketConfiguration(Bandwidth)을 분석하여 TTL을 자동 관리합니다.
        return LettuceBasedProxyManager.builderFor(connection)
                .build();
    }
}