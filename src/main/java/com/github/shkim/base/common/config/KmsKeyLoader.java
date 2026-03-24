package com.github.shkim.base.common.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.web.client.RestClient;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 애플리케이션 기동 시 KMS(키 관리 서버)와 통신하여 도메인별 암호화 키 캐싱 로직 수행.
 * <p>
 * KmsProperties를 주입받아 설정값을 활용하며, RestClient로 외부 통신 수행
 * </p>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class KmsKeyLoader implements ApplicationRunner {

    private final KmsProperties kmsProperties;
    private final RestClient restClient;

    public static final ConcurrentHashMap<String, String> KEY_CACHE = new ConcurrentHashMap<>();

    /**
     * 스프링 컨텍스트 로드 완료 직후 KMS 서버 연동 및 키 적재 수행
     *
     * @param args 실행 시 전달받는 커맨드라인 인수
     */
    @Override
    public void run(ApplicationArguments args) {
        log.info("[KMS] 기동 시 암복호화 키 로드 시작... (KMS Server: {})", kmsProperties.serverUrl());

        if (CollectionUtils.isEmpty(kmsProperties.keySets())) {
            log.warn("[KMS] application.yml에 설정된 key-sets가 존재하지 않습니다.");
            return;
        }

        for (Map.Entry<String, String> entry : kmsProperties.keySets().entrySet()) {
            String domain = entry.getKey();
            String keySetId = entry.getValue();

            try {
                // 실제 환경에서는 restClient를 사용해 API 호출
                String actualKey = "MOCK_SECRET_KEY_FOR_" + keySetId;

                KEY_CACHE.put(domain, actualKey);
                log.info("[KMS] {} 도메인 키 적재 완료 (KeySetId: {})", domain, keySetId);
            } catch (Exception e) {
                log.error("[KMS] {} 도메인 키 적재 실패.", domain, e);
                throw new IllegalStateException("KMS Key Load Failed");
            }
        }
    }
}