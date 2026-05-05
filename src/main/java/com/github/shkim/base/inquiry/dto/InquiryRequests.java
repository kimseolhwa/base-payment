package com.github.shkim.base.inquiry.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

/**
 * 조회(Inquiry) 도메인 API 요청 DTO 모음.
 * <p>
 * Java 21 record를 활용하여 불변 객체 보장 및 파라미터 검증(@Valid) 로직 간결화 수행
 * </p>
 */
public class InquiryRequests {

    /**
     * 가맹점 ID와 주문 ID가 모두 필요한 결제 상세 내역 조회 요청 DTO
     *
     * @param merchantId 가맹점 식별 ID
     * @param orderId 주문 식별 ID
     */
    @Schema(description = "결제 상세 내역 조회 요청 DTO")
    public record PaymentDetailReq(
            @Schema(description = "가맹점 식별 ID", example = "TEST_M001")
            @NotBlank(message = "가맹점 ID(merchantId)는 필수입니다.") String merchantId,
            
            @Schema(description = "주문 식별 ID", example = "ORD202603230001")
            @NotBlank(message = "주문 ID(orderId)는 필수입니다.") String orderId
    ) {}

    /**
     * 가맹점 ID만 필요한 상점 환경 정보 조회 요청 DTO (확장성 예시)
     */
    @Schema(description = "상점 정보 조회 요청 DTO")
    public record MerchantInfoReq(
            @Schema(description = "가맹점 식별 ID", example = "TEST_M001")
            @NotBlank(message = "가맹점 ID(merchantId)는 필수입니다.") String merchantId
    ) {}
}