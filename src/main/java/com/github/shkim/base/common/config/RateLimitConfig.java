package com.github.shkim.base.common.config;

import com.github.shkim.base.common.interceptor.RateLimitInterceptor;
import io.github.bucket4j.distributed.proxy.ProxyManager;
import io.github.bucket4j.redis.lettuce.cas.LettuceBasedProxyManager;
import io.lettuce.core.RedisClient;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.codec.ByteArrayCodec;
import io.lettuce.core.codec.RedisCodec;
import io.lettuce.core.codec.StringCodec;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * 처리율 제한(Rate Limit) 인터셉터를 등록하고
 * Bucket4j용 Redis ProxyManager를 설정하는 클래스
 */
@Configuration
@RequiredArgsConstructor
public class RateLimitConfig implements WebMvcConfigurer {

    private final RateLimitInterceptor rateLimitInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 결제 등 중요한 API 엔드포인트에만 적용 (필요에 따라 경로 추가/제외)
        registry.addInterceptor(rateLimitInterceptor)
                .addPathPatterns("/api/v1/noti/**", "/api/v1/inquiry/**");
    }
}