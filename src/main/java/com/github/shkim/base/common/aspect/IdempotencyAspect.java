package com.github.shkim.base.common.aspect;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.shkim.base.common.exception.IdempotencyException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

/**
 * Idempotent 애노테이션이 부여된 API의 멱등성을 보장하는 AOP 클래스.
 * <p>
 * HTTP 헤더의 Idempotency-Key를 추출하여 Redis 기반 분산 락(Lock)을 획득하고,
 * 최초 요청은 실행 후 결과를 캐싱하며, 중복 요청은 캐싱된 이전 응답을 즉시 반환 처리
 * </p>
 */
@Aspect
@Component
@Slf4j
@RequiredArgsConstructor
public class IdempotencyAspect {

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

    private static final String IDEMPOTENCY_HEADER = "Idempotency-Key";
    private static final String REDIS_PREFIX = "idempotency:";
    private static final String STATUS_PROCESSING = "PROCESSING";
    private static final long EXPIRE_HOURS = 24L; // 멱등성 보장 기간 (24시간)

    /**
     * 멱등성 애노테이션이 붙은 메서드 주변(Around)에서 검증 로직 수행.
     *
     * @param joinPoint AOP 조인 포인트
     * @return 캐싱된 이전 결과 또는 신규 비즈니스 로직 실행 결과
     * @throws Throwable 메서드 실행 중 예외 발생 시
     */
    @Around("@annotation(com.github.shkim.base.common.annotation.Idempotent)")
    public Object checkIdempotency(ProceedingJoinPoint joinPoint) throws Throwable {
        HttpServletRequest request = getCurrentHttpRequest();
        String idempotencyKey = request.getHeader(IDEMPOTENCY_HEADER);

        // 1. 헤더 검증
        if (!StringUtils.hasText(idempotencyKey)) {
            log.warn("[Idempotency] 필수 헤더 누락 (Idempotency-Key)");
            throw new IdempotencyException("멱등성 키(Idempotency-Key)가 누락되었습니다.");
        }

        String redisKey = REDIS_PREFIX + idempotencyKey;

        // 2. Redis 분산 락 획득 시도 (setIfAbsent: 키가 없으면 저장하고 true, 있으면 false 반환)
        Boolean isFirstRequest = redisTemplate.opsForValue()
                .setIfAbsent(redisKey, STATUS_PROCESSING, Duration.ofMinutes(5));

        if (Boolean.FALSE.equals(isFirstRequest)) {
            // 3. 이미 동일한 키로 요청이 들어온 경우 (따닥 클릭 or 과거 성공 건)
            String previousData = redisTemplate.opsForValue().get(redisKey);

            if (STATUS_PROCESSING.equals(previousData)) {
                log.warn("[Idempotency] 동시 중복 요청 감지 - Key: {}", idempotencyKey);
                throw new IdempotencyException("현재 처리 중인 요청입니다. 잠시 후 다시 시도해주세요.");
            }

            // 과거에 이미 완료된(성공한) 요청이라면 로직을 타지 않고 기존 응답 객체를 역직렬화하여 즉시 반환 (단, 상태 코드는 200 OK)
            log.info("[Idempotency] 과거 성공 요청 재인입 - 캐싱된 응답 반환. Key: {}", idempotencyKey);
            MethodSignature signature = (MethodSignature) joinPoint.getSignature();
            Class<?> returnType = signature.getReturnType();
            return objectMapper.readValue(previousData, returnType);
        }

        // 4. 최초 요청일 경우 실제 비즈니스 로직(Controller) 실행
        Object result;
        try {
            result = joinPoint.proceed();
        } catch (Exception e) {
            // 실패 시 다른 요청이 다시 시도할 수 있도록 락(키) 해제
            redisTemplate.delete(redisKey);
            throw e;
        }

        // 5. 성공적으로 완료된 경우, 응답 객체를 직렬화하여 Redis에 24시간 동안 덮어쓰기 저장
        String serializedResult = objectMapper.writeValueAsString(result);
        redisTemplate.opsForValue().set(redisKey, serializedResult, EXPIRE_HOURS, TimeUnit.HOURS);

        return result;
    }

    /**
     * 현재 스레드의 HTTP Request 컨텍스트 추출.
     *
     * @return 현재 요청의 HttpServletRequest 객체
     */
    private HttpServletRequest getCurrentHttpRequest() {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes == null) {
            throw new IllegalStateException("HTTP 요청 컨텍스트를 찾을 수 없습니다.");
        }
        return attributes.getRequest();
    }
}