package com.github.shkim.base.common.aspect;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zaxxer.hikari.HikariDataSource;
import com.github.shkim.base.common.util.MaskingUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.StopWatch;

import javax.sql.DataSource;

/**
 * 애플리케이션의 주요 지점(Controller, MyBatis Mapper)의 실행 시간 및 상태를 기록하는 AOP 클래스.
 * <p>
 * API 응답 속도 모니터링 및 DB 쿼리 실행 추적(병목 현상, 커넥션 풀 상태)을 목적으로 운영
 * SQL 관련 로그는 분리된 파일(sql-*.log)로 라우팅
 * </p>
 */
@Aspect
@Component
@Slf4j
@RequiredArgsConstructor
public class PerformanceLoggingAspect {

    private final DataSource dataSource;
    private final ObjectMapper objectMapper;
    private static final Logger sqlLogger = LoggerFactory.getLogger("SQL_TRACE_LOGGER");

    /**
     * Controller 레이어의 API/UI 요청 처리 시간을 측정하여 로깅
     *
     * @param joinPoint AOP 어드바이스가 적용될 조인 포인트 (메서드 실행 정보)
     * @return Controller 메서드의 실행 결과
     * @throws Throwable 메서드 실행 중 발생한 예외
     */
    @Around("execution(* com.github.shkim.base..presentation.*Controller.*(..))")
    public Object logController(ProceedingJoinPoint joinPoint) throws Throwable {
        StopWatch stopWatch = new StopWatch();
        stopWatch.start(); // 측정 시작

        try {
            return joinPoint.proceed();
        } finally {
            stopWatch.stop(); // 측정 종료
            log.info("[API/UI] {} - Time: {} ms", joinPoint.getSignature().toShortString(), stopWatch.getTotalTimeMillis());
        }
    }

    /**
     * MyBatis Mapper 인터페이스의 실행 시간, 커넥션 풀 상태, 요청/응답 데이터를 로깅
     * <p>
     * 쿼리 실행 중 예외가 발생하더라도 즉각적으로 에러를 로깅하고 원본 예외를 던져 트랜잭션 롤백 유도
     * </p>
     *
     * @param joinPoint AOP 어드바이스가 적용될 조인 포인트
     * @return DB 쿼리 실행 결과 (결과셋 또는 반영 건수)
     * @throws Throwable DB 쿼리 실행 중 발생한 예외
     */
    @Around("execution(* com.github.shkim.base..infrastructure.*Mapper.*(..))")
    public Object logDB(ProceedingJoinPoint joinPoint) throws Throwable {
        StopWatch stopWatch = new StopWatch();

        // [1] 요청 쿼리 파라미터 추출 및 마스킹
        Object[] args = joinPoint.getArgs();
        String requestData = extractAndMaskData(args);

        Object result;
        stopWatch.start();
        try {
            result = joinPoint.proceed();
        } catch (Exception e) {
            sqlLogger.error("[DB Error] 쿼리 실행 중 예외 발생: {}", e.getMessage(), e);
            throw e;
        } finally {
            stopWatch.stop(); // 예외가 발생하든 안 하든 타이머 정지
        }

        // [2] 응답 데이터(Result) 추출 및 마스킹
        String responseData = extractAndMaskData(result);

        // [3] HikariCP 커넥션 풀 상태
        String poolStatus = "Unknown";
        if (dataSource instanceof HikariDataSource hikari) {
            poolStatus = String.format("Active: %d, Idle: %d", hikari.getHikariPoolMXBean().getActiveConnections(), hikari.getHikariPoolMXBean().getIdleConnections());
        }

        // [4] SQL 전용 로거로 한 줄에 깔끔하게 출력
        sqlLogger.info("[DB Query] {} | Time: {}ms | Pool: [{}] | Req: {} | Res: {}", joinPoint.getSignature().toShortString(), stopWatch.getTotalTimeMillis(), poolStatus, requestData, responseData);

        return result;
    }

    /**
     * 파라미터나 결과를 JSON 형태의 단일 문자열로 변환하고, 민감정보 마스킹 처리
     *
     * @param data 직렬화할 객체 (요청 Args 또는 응답 Result)
     * @return 직렬화 및 마스킹이 완료된 문자열
     */
    private String extractAndMaskData(Object data) {
        if (data == null) return "null";
        try {
            // ObjectMapper로 JSON 변환 후 마스킹 (Map 변환 없이 문자열 정규식 기반 치환)
            String jsonStr = objectMapper.writeValueAsString(data);
            return MaskingUtil.logMasking(jsonStr, ", ");
        } catch (Exception e) {
            return "Unserializable Data";
        }
    }
}