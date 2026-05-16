package com.github.shkim.base.common.interceptor;

import com.github.shkim.base.common.exception.RateLimitException;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.BucketConfiguration;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.distributed.proxy.ProxyManager;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.time.Duration;
import java.util.function.Supplier;

/**
 * API 호출 처리율을 제한하는 인터셉터
 * IP + URI 조합을 기준으로 Redis(분산 환경)에서 초당 호출 횟수를 제한합니다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RateLimitInterceptor implements HandlerInterceptor {

    // 로컬 맵(cache) 대신 Redis 연동을 담당하는 ProxyManager 주입
    private final ProxyManager<String> proxyManager;

    // 초당 10번 호출 허용 (버킷 설정)
    // Bucket4j 8.x 이상에서 권장하는 Builder 패턴 (Bandwidth.builder() 사용)
    private final Supplier<BucketConfiguration> configurationSupplier = () -> BucketConfiguration.builder()
            .addLimit(Bandwidth.builder()
                    .capacity(10)
                    .refillGreedy(10, Duration.ofSeconds(1))
                    .build())
            .build();

    @Override
    public boolean preHandle(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull Object handler) {
        String clientIp = getClientIp(request);
        String uri = request.getRequestURI();
        
        // IP와 요청 URI를 조합하여 Redis 키를 생성 (예: rate_limit:127.0.0.1:/api/v1/inquiry/payment)
        String bucketKey = "rate_limit:" + clientIp + ":" + uri;

        // ProxyManager를 통해 Redis에 저장된 버킷 조회 (없으면 생성)
        // Bucket4j 8.x 이상에서는 builder().build()에 Supplier를 넘기는 방식(build(key, Supplier))을 권장
        Bucket bucket = proxyManager.builder().build(bucketKey, configurationSupplier);

        if (bucket.tryConsume(1)) {
            return true;
        } else {
            log.warn("Rate limit exceeded for IP: {}, URI: {}", clientIp, uri);
            throw new RateLimitException("Too many requests from IP: " + clientIp);
        }
    }

    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        return ip;
    }
}