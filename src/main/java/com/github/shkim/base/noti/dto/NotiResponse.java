package com.github.shkim.base.noti.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Builder
@Schema(description = "노티 발송 응답 DTO")
public record NotiResponse(
        @Schema(description = "결과 상태 코드", example = "0000")
        String resCd, 
        
        @Schema(description = "결과 상세 메시지", example = "노티 처리가 완료되었습니다.")
        String resMsg
) {}