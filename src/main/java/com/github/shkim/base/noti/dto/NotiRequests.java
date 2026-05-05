package com.github.shkim.base.noti.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public class NotiRequests {

    /**
     * 노티 발송 요청 파라미터 레코드
     */
    @Schema(description = "노티 발송 요청 DTO")
    public record SendReq(
            @Schema(description = "가맹점 식별 ID", example = "TEST_M001")
            @NotBlank(message = "가맹점 ID는 필수입니다.") String merchantId,
            
            @Schema(description = "주문 식별 ID", example = "ORD202603230001")
            @NotBlank(message = "주문 ID는 필수입니다.") String orderId,
            
            @Schema(description = "결제 금액", example = "50000")
            @NotNull(message = "결제 금액은 필수입니다.") BigDecimal amount,
            
            @Schema(description = "결제 상태", example = "SUCCESS")
            @NotBlank(message = "결제 상태는 필수입니다.") String status
    ) {}
}