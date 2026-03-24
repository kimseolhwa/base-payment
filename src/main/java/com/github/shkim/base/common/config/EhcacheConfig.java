package com.github.shkim.base.common.config;

import org.ehcache.config.builders.CacheConfigurationBuilder;
import org.ehcache.config.builders.ExpiryPolicyBuilder;
import org.ehcache.config.builders.ResourcePoolsBuilder;
import org.ehcache.jsr107.Eh107Configuration;
import org.springframework.boot.autoconfigure.cache.JCacheManagerCustomizer;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

/**
 * 모던 자바(Java 21) 표준에 맞춘 프로그래밍 방식의 Ehcache 3 설정 클래스.
 * <p>
 * 레거시 XML 설정 및 JAXB 의존성을 배제하고,
 * 타입 세이프(Type-safe)한 Java Config 기반으로 JSR-107 캐시 정책 정의 수행
 * </p>
 */
@EnableCaching
@Configuration
public class EhcacheConfig {

    /**
     * 스프링 부트의 JCacheManager 커스터마이저 빈 등록.
     * <p>
     * 기동 시점에 'paymentCache' 등 필요한 캐시 공간과 정책(TTL, 힙 크기)을 메모리에 할당 수행
     * </p>
     *
     * @return 커스터마이징된 JCacheManager 설정 객체
     */
    @Bean
    public JCacheManagerCustomizer cacheManagerCustomizer() {
        return cacheManager -> {
            // 1. Ehcache 3 전용 캐시 설정 빌더 생성
            org.ehcache.config.CacheConfiguration<String, Object> ehcacheConfig =
                    CacheConfigurationBuilder.newCacheConfigurationBuilder(
                                    String.class, Object.class,
                                    ResourcePoolsBuilder.heap(100) // 힙 메모리에 최대 100개 데이터 유지
                            )
                            .withExpiry(ExpiryPolicyBuilder.timeToLiveExpiration(Duration.ofMinutes(10))) // 10분 후 만료(TTL)
                            .build();

            // 2. JSR-107 표준 호환 래퍼를 통해 캐시 매니저에 등록
            cacheManager.createCache("paymentCache",
                    Eh107Configuration.fromEhcacheCacheConfiguration(ehcacheConfig));

            // TODO: 추후 다른 API 도메인에서 캐시가 필요하면 여기에 추가 생성 가능
        };
    }
}