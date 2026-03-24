package com.github.shkim.base.noti.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public class NotiRequests {

    /**
     * 노티 발송 요청 파라미터 레코드
     */
    public record SendReq(
            @NotBlank(message = "가맹점 ID는 필수입니다.") String merchantId,
            @NotBlank(message = "주문 ID는 필수입니다.") String orderId,
            @NotNull(message = "결제 금액은 필수입니다.") BigDecimal amount,
            @NotBlank(message = "결제 상태는 필수입니다.") String status
    ) {}
}