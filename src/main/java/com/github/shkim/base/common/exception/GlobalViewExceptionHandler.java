package com.github.shkim.base.common.exception;

import com.github.shkim.base.common.util.MessageUtils;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.ModelAndView;

/**
 * UI(View) 화면 전용 예외 처리 어드바이스.
 * <p>
 * View 화면을 렌더링하는 도메인(cert 등)에서 발생하는 예외를 캐치하여
 * 지정된 Thymeleaf 에러 페이지로 포워딩
 * </p>
 */
@Slf4j
@ControllerAdvice(basePackages = "com.github.shkim.base.cert")
@RequiredArgsConstructor
public class GlobalViewExceptionHandler {

    private final MessageUtils messageUtils;

    @ExceptionHandler(DataNotFoundException.class)
    public ModelAndView handleDataNotFoundException(DataNotFoundException e, HttpServletRequest request) {
        log.warn("[View Data Not Found] {}", e.getMessage());
        String resMsg = messageUtils.getMessage(ResponseCode.BAD_REQUEST.getMessageKey());
        return createErrorView(HttpStatus.BAD_REQUEST, ResponseCode.BAD_REQUEST.getCode(), resMsg, "error/400");
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ModelAndView handleValidationException(MethodArgumentNotValidException e, HttpServletRequest request) {
        String errorMessage = e.getBindingResult().getAllErrors().getFirst().getDefaultMessage();
        log.warn("[View Validation Error] {}", errorMessage);
        return createErrorView(HttpStatus.BAD_REQUEST, ResponseCode.VALIDATION_ERROR.getCode(), errorMessage, "error/400");
    }

    @ExceptionHandler(IdempotencyException.class)
    public ModelAndView handleIdempotencyException(IdempotencyException e, HttpServletRequest request) {
        log.warn("[View Idempotency Error] {}", e.getMessage());
        String resMsg = messageUtils.getMessage(ResponseCode.CONFLICT.getMessageKey());
        return createErrorView(HttpStatus.CONFLICT, ResponseCode.CONFLICT.getCode(), resMsg, "error/400");
    }

    @ExceptionHandler(Exception.class)
    public ModelAndView handleAllException(Exception e, HttpServletRequest request) {
        String traceId = (String) request.getAttribute("traceId");
        log.error("[View System Error] TraceID: {} | URI: {} {} | Message: {}",
                traceId, request.getMethod(), request.getRequestURI(), e.getMessage(), e);

        String resMsg = messageUtils.getMessage(ResponseCode.INTERNAL_SERVER_ERROR.getMessageKey());
        return createErrorView(HttpStatus.INTERNAL_SERVER_ERROR, ResponseCode.INTERNAL_SERVER_ERROR.getCode(), resMsg, "error/500");
    }

    private ModelAndView createErrorView(HttpStatus status, String resCd, String resMsg, String viewName) {
        ModelAndView mav = new ModelAndView(viewName);
        mav.addObject("resCd", resCd);
        mav.addObject("resMsg", resMsg);
        mav.setStatus(status);
        return mav;
    }
}