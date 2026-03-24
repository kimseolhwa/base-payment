package com.github.shkim.base.common.exception;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.ModelAndView;

import java.util.HashMap;
import java.util.Map;

/**
 * 전역 예외 처리 및 에러 응답(API/UI) 라우팅 컨트롤러 어드바이스.
 * <p>
 * 시스템 전반에서 발생하는 예외를 캐치하여 클라이언트의 요청 타입(JSON API 또는 HTML View)에 맞게
 * 표준화된 에러 규격으로 변환하여 응답
 * </p>
 */
@Slf4j
@ControllerAdvice // @RestControllerAdvice 대신 뷰 리턴을 위해 @ControllerAdvice 사용
public class GlobalExceptionHandler {

    /**
     * DB 조회 결과 부재 등 데이터 미존재 예외 처리 (400 Bad Request)
     *
     * @param e       발생한 DataNotFoundException
     * @param request HTTP 요청 객체
     * @return 에러 응답 객체 (ResponseEntity 또는 ModelAndView)
     */
    @ExceptionHandler(DataNotFoundException.class)
    public Object handleDataNotFoundException(DataNotFoundException e, HttpServletRequest request) {
        log.warn("[Data Not Found] {}", e.getMessage());
        return routeErrorResponse(request, HttpStatus.BAD_REQUEST, "4000", e.getMessage(), "error/400");
    }

    /**
     * 클라이언트 요청 파라미터 유효성 검증 실패 예외 처리
     *
     * @param e       발생한 MethodArgumentNotValidException
     * @param request HTTP 요청 객체
     * @return 에러 응답 객체
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Object handleValidationException(MethodArgumentNotValidException e, HttpServletRequest request) {
        String errorMessage = e.getBindingResult().getAllErrors().get(0).getDefaultMessage();
        log.warn("[Validation Error] {}", errorMessage);
        return routeErrorResponse(request, HttpStatus.BAD_REQUEST, "4001", errorMessage, "error/400");
    }

    /**
     * 멱등성 검증 실패 및 동시 중복 요청 예외 처리 (409 Conflict)
     *
     * @param e 발생한 IdempotencyException
     * @param request HTTP 요청 객체
     * @return 에러 응답 객체
     */
    @ExceptionHandler(IdempotencyException.class)
    public Object handleIdempotencyException(IdempotencyException e, HttpServletRequest request) {
        log.warn("[Idempotency Error] {}", e.getMessage());
        return routeErrorResponse(request, HttpStatus.CONFLICT, "4090", e.getMessage(), "error/400");
    }

    /**
     * 처리되지 않은 최상위 시스템 예외 처리 (500 Internal Server Error)
     *
     * @param e       발생한 최상위 Exception
     * @param request HTTP 요청 객체
     * @return 에러 응답 객체
     */
    @ExceptionHandler(Exception.class)
    public Object handleAllException(Exception e, HttpServletRequest request) {
        log.error("[System Error] {}", e.getMessage(), e);
        return routeErrorResponse(request, HttpStatus.INTERNAL_SERVER_ERROR, "9999", "서버 장애가 발생했습니다.", "error/500");
    }

    /**
     * 요청 헤더 및 URI 분석을 통해 API(JSON) 응답과 UI(HTML) 응답 분기 처리
     *
     * @param request  HTTP 요청 객체
     * @param status   HTTP 상태 코드
     * @param resCd    비즈니스 에러 코드
     * @param resMsg   비즈니스 에러 메시지
     * @param viewName HTML 응답 시 포워딩할 타임리프 뷰 경로
     * @return 분기 처리된 최종 응답 객체
     */
    private Object routeErrorResponse(HttpServletRequest request, HttpStatus status, String resCd, String resMsg, String viewName) {

        String acceptHeader = request.getHeader(HttpHeaders.ACCEPT);

        boolean isApiRequest = request.getRequestURI().startsWith("/api/") ||
                (StringUtils.hasText(acceptHeader) && acceptHeader.contains(MediaType.APPLICATION_JSON_VALUE));

        if (isApiRequest) {
            // 1. API 요청이면 JSON 응답
            Map<String, Object> response = new HashMap<>();
            response.put("res_cd", resCd);
            response.put("res_msg", resMsg);
            return new ResponseEntity<>(response, status);
        } else {
            // 2. UI 화면 요청이면 Thymeleaf 공통 에러 페이지 리턴
            ModelAndView mav = new ModelAndView(viewName);
            mav.addObject("resCd", resCd);
            mav.addObject("resMsg", resMsg);
            mav.setStatus(status);
            return mav;
        }
    }
}