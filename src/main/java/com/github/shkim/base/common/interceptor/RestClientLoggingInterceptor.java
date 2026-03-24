package com.github.shkim.base.common.interceptor;

import com.github.shkim.base.common.util.MaskingUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.StopWatch;
import org.springframework.util.StreamUtils;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * 외부 API(VAN, KMS 등) 연동 시 요청/응답 전문 공통 로깅 인터셉터.
 * <p>
 * 통신 소요 시간(StopWatch) 측정 및 송수신 페이로드 마스킹 처리 수행
 * </p>
 */
@Slf4j
@Component
public class RestClientLoggingInterceptor implements ClientHttpRequestInterceptor {

    /**
     * HTTP 클라이언트 요청 전/후 가로채기 로직 수행
     *
     * @param request 외부로 전송될 HTTP 요청 정보
     * @param body 요청 본문(Payload) 바이트 배열
     * @param execution 실제 요청 실행을 위한 컨텍스트
     * @return 외부 API로부터 수신된 HTTP 응답 객체
     * @throws IOException 통신 중 입출력 에러 발생 시
     */
    @Override
    public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution) throws IOException {

        // 1. 요청 전 로깅 (Request)
        String requestBody = new String(body, StandardCharsets.UTF_8);
        String maskedReqBody = getMaskedPayload(requestBody);

        log.info("[외부 API 요청] URI: {} | Method: {} | Headers: {} | Body: {}",
                request.getURI(), request.getMethod(), request.getHeaders(), maskedReqBody);

        StopWatch stopWatch = new StopWatch();
        stopWatch.start();

        // 2. 실제 통신 실행
        ClientHttpResponse response;
        try {
            response = execution.execute(request, body);
        } finally {
            stopWatch.stop(); // 외부 연동이 실패(Timeout 등)하더라도 걸린 시간을 정확히 기록
        }

        // 3. 응답 후 로깅 (Response)
        // BufferingClientHttpRequestFactory 덕분에 body를 읽어도 스트림이 닫히지 않음
        String responseBody = StreamUtils.copyToString(response.getBody(), StandardCharsets.UTF_8);
        String maskedResBody = getMaskedPayload(responseBody);

        log.info("[외부 API 응답] URI: {} | Status: {} | Time: {}ms | Body: {}",
                request.getURI(), response.getStatusCode(), stopWatch.getTotalTimeMillis(), maskedResBody);

        return response;
    }

    /**
     * 페이로드 유효성 검사 및 마스킹 처리
     *
     * @param payload 원본 문자열 데이터
     * @return 마스킹 처리된 데이터 또는 예외 발생 시 대체 문자열 반환
     */
    private String getMaskedPayload(String payload) {
        if (!StringUtils.hasText(payload)) return "";
        try {
            return MaskingUtil.logMasking(payload, "&");
        } catch (Exception e) {
            return "Unparseable Payload";
        }
    }
}