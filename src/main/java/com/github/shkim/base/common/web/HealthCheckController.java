package com.github.shkim.base.common.web;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * L4 로드밸런서 및 인프라 모니터링을 위한 헬스체크 컨트롤러.
 * <p>
 * 물리 파일 존재 여부(L4 제어용) 및 DB 커넥션 풀 상태를 동시 점검하여
 * 현재 서버의 정상 서비스 가능 여부 반환
 * </p>
 */
@Slf4j
@RestController
@RequiredArgsConstructor
public class HealthCheckController {

    private final JdbcTemplate jdbcTemplate;

    // application.yml에서 파일 경로를 읽어옴 (기본값: 프로젝트 루트의 l4check.txt)
    @Value("${health.check.file.path:./l4check.txt}")
    private String healthCheckFilePath;

    /**
     * 헬스체크 파일 시스템 및 DB 커넥션 검증 로직.
     * <p>
     * 파일 미존재 시 무중단 배포를 위한 배포 제외 상태(503) 리턴,
     * DB 에러 시 시스템 장애 상태(503) 리턴
     * </p>
     *
     * @return 정상 작동 시 "OK" (200), 실패 시 사유와 함께 503 코드 반환
     */
    @GetMapping("/health")
    public ResponseEntity<String> checkHealth() {
        // 1. L4 상태 관리용 파일 존재 여부 확인 (파일이 없으면 L4에서 제외됨)
        Path healthFile = Paths.get(healthCheckFilePath);
        if (!Files.exists(healthFile)) {
            log.warn("[Health Check] L4 check file not found. Path: {}", healthCheckFilePath);
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body("MAINTENANCE");
        }

        // 2. DB 커넥션 정상 여부 확인
        try {
            Integer result = jdbcTemplate.queryForObject("SELECT 1", Integer.class);
            if (result != null && result == 1) {
                return ResponseEntity.ok("OK"); // 파일 존재 + DB 정상 = 200 OK
            } else {
                throw new RuntimeException("DB returned unexpected result.");
            }
        } catch (Exception e) {
            log.error("[Health Check] DB connection failed.", e);
            // DB 장애 발생 시에도 503 에러를 뱉어 L4에서 자동 제외되도록 처리
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body("DB_ERROR");
        }
    }
}