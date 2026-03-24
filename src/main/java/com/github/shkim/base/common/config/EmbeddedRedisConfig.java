package com.github.shkim.base.common.config;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import redis.embedded.RedisServer;

import java.io.IOException;

/**
 * 로컬 개발 환경(local) 전용 내장 레디스(Embedded Redis) 설정 클래스.
 * <p>
 * 별도의 Redis 서버 설치 없이 스프링 부트 라이프사이클에 맞춰
 * 인메모리 Redis 서버를 자동 기동 및 종료 처리 수행
 * </p>
 */
@Slf4j
@Profile("local") // local 프로파일에서만 빈으로 등록되어 실행됨
@Configuration
public class EmbeddedRedisConfig {

    @Value("${spring.data.redis.port:6379}")
    private int redisPort;

    private RedisServer redisServer;

    /**
     * 스프링 빈(Bean) 초기화 시점에 내장 레디스 서버 실행 수행.
     */
    @PostConstruct
    public void startRedis() throws IOException {
        // 이미 지정된 포트로 실행 중인 프로세스가 없을 때만 실행
        redisServer = new RedisServer(redisPort);
        try {
            redisServer.start();
            log.info("[Embedded Redis] 로컬 내장 레디스 서버가 {} 포트로 시작되었습니다.", redisPort);
        } catch (Exception e) {
            log.warn("[Embedded Redis] 기동 실패 (이미 레디스가 실행 중일 수 있습니다): {}", e.getMessage());
        }
    }

    /**
     * 스프링 컨테이너 종료 시점에 내장 레디스 서버 안전 종료 수행.
     */
    @PreDestroy
    public void stopRedis() {
        if (redisServer != null) {
            try {
                redisServer.stop();
                log.info("[Embedded Redis] 로컬 내장 레디스 서버가 성공적으로 종료되었습니다.");
            } catch (Exception e) {
                // 종료 중 발생하는 예외는 서버 셧다운에 영향을 주지 않도록 로그만 남기고 삼킴
                log.error("[Embedded Redis] 레디스 서버 종료 중 오류 발생: {}", e.getMessage());
            }
        }
    }
}