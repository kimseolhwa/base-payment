package com.github.shkim.base.common.exception;

import com.github.shkim.base.common.util.MessageUtils;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

/**
 * REST API(inquiry, noti 등) 전용 예외 처리 어드바이스.
 * <p>
 * API 경로(/api/**)에서 발생하는 예외를 캐치하여 JSON 형태로 응답
 * </p>
 */
@Slf4j
@RestControllerAdvice(basePackages = {
        "com.github.shkim.base.inquiry",
        "com.github.shkim.base.noti"
})
@RequiredArgsConstructor
public class GlobalExceptionHandler {

    private final MessageUtils messageUtils;

    @ExceptionHandler(DataNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleDataNotFoundException(DataNotFoundException e, HttpServletRequest request) {
        log.warn("[API Data Not Found] {}", e.getMessage());
        String resMsg = messageUtils.getMessage(ResponseCode.BAD_REQUEST.getMessageKey());
        return createJsonResponse(HttpStatus.BAD_REQUEST, ResponseCode.BAD_REQUEST.getCode(), resMsg);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidationException(MethodArgumentNotValidException e, HttpServletRequest request) {
        String errorMessage = e.getBindingResult().getAllErrors().getFirst().getDefaultMessage();
        log.warn("[API Validation Error] {}", errorMessage);
        return createJsonResponse(HttpStatus.BAD_REQUEST, ResponseCode.VALIDATION_ERROR.getCode(), errorMessage);
    }

    @ExceptionHandler(IdempotencyException.class)
    public ResponseEntity<Map<String, Object>> handleIdempotencyException(IdempotencyException e, HttpServletRequest request) {
        log.warn("[API Idempotency Error] {}", e.getMessage());
        String resMsg = messageUtils.getMessage(ResponseCode.CONFLICT.getMessageKey());
        return createJsonResponse(HttpStatus.CONFLICT, ResponseCode.CONFLICT.getCode(), resMsg);
    }

    // 처리율 제한 초과 예외 처리 (429 Too Many Requests)
    @ExceptionHandler(RateLimitException.class)
    public ResponseEntity<Map<String, Object>> handleRateLimitException(RateLimitException e, HttpServletRequest request) {
        log.warn("[API Rate Limit Exceeded] {}", e.getMessage());
        return createJsonResponse(HttpStatus.TOO_MANY_REQUESTS, "4290", "Too many requests. Please try again later.");
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleAllException(Exception e, HttpServletRequest request) {
        String traceId = (String) request.getAttribute("traceId");
        log.error("[API System Error] TraceID: {} | URI: {} {} | Message: {}",
                traceId, request.getMethod(), request.getRequestURI(), e.getMessage(), e);

        String resMsg = messageUtils.getMessage(ResponseCode.INTERNAL_SERVER_ERROR.getMessageKey());
        return createJsonResponse(HttpStatus.INTERNAL_SERVER_ERROR, ResponseCode.INTERNAL_SERVER_ERROR.getCode(), resMsg);
    }

    private ResponseEntity<Map<String, Object>> createJsonResponse(HttpStatus status, String resCd, String resMsg) {
        Map<String, Object> response = new HashMap<>();
        response.put("res_cd", resCd);
        response.put("res_msg", resMsg);
        return new ResponseEntity<>(response, status);
    }
}