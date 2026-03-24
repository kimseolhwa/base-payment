package com.github.shkim.base.inquiry.controller;

import com.github.shkim.base.inquiry.service.InquiryService;
import com.github.shkim.base.inquiry.dto.InquiryRequests;
import com.github.shkim.base.inquiry.dto.InquiryResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 시스템 내 각종 데이터 조회를 담당하는 REST API 컨트롤러.
 * <p>
 * 클라이언트의 요청 파라미터 유효성 검증 및 서비스 레이어 위임 수행
 * </p>
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/inquiry")
@RequiredArgsConstructor
public class InquiryController {

    private final InquiryService inquiryService;

    /**
     * 주문 ID 기반 결제 상세 내역 조회 수행.
     *
     * @param request 가맹점 ID와 주문 ID가 포함된 검증 완료 레코드 객체
     * @return 조회 결과가 포장된 HTTP 200 응답
     */
    @PostMapping("/payment")
    public ResponseEntity<InquiryResponse> getPaymentDetail(@Valid @RequestBody InquiryRequests.PaymentDetailReq request) {
        log.info("[Inquiry] 결제 내역 조회 요청: merchantId={}, orderId={}", request.merchantId(), request.orderId());

        InquiryResponse response = inquiryService.getPaymentDetail(request);
        return ResponseEntity.ok(response);
    }
}