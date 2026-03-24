package com.github.shkim.base.noti.service;

import com.github.shkim.base.noti.dto.NotiRequests;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

/**
 * 외부 시스템 HTTP 통신 및 장애 격리(서킷 브레이커)를 전담하는 클라이언트.
 */
@Slf4j
@Component
public class ExternalNotiClient {

    private final RestClient restClient;

    public ExternalNotiClient(RestClient.Builder restClientBuilder) {
        this.restClient = restClientBuilder.build();
    }

    /**
     * 외부 가맹점 서버로 HTTP 통신 수행. (실패 시 서킷 브레이커 개입)
     */
    @CircuitBreaker(name = "externalApi", fallbackMethod = "fallbackExternalApi")
    public String callExternalApi(NotiRequests.SendReq request) {
        log.info("[Noti Client] 외부 가맹점 서버로 HTTP 전송 시도...");

        return restClient.post()
                .uri("http://localhost:9999/dummy-endpoint") // 의도된 장애 유발 포트
                .body(request)
                .retrieve()
                .body(String.class);
    }

    /**
     * 통신 실패 또는 서킷 브레이커 차단(Open) 시 실행되는 Fallback.
     */
    private String fallbackExternalApi(NotiRequests.SendReq request, Throwable t) {
        log.error("[Noti Fallback] 외부 통신 실패 또는 차단됨! 사유: {}", t.getMessage());
        return "FALLBACK_SUCCESS (지연 발송 대기)";
    }
}