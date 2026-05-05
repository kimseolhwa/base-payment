package com.github.shkim.base.noti.controller;

import com.github.shkim.base.noti.service.NotiService;
import com.github.shkim.base.noti.dto.NotiRequests;
import com.github.shkim.base.noti.dto.NotiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 외부 시스템으로의 노티(알림) 발송을 담당하는 컨트롤러.
 */
@Tag(name = "Noti API", description = "결제 노티(알림) 발송 관련 API")
@Slf4j
@RestController
@RequestMapping("/api/v1/noti")
@RequiredArgsConstructor
public class NotiController {

    private final NotiService notiService;

    @Operation(summary = "결제 노티 발송", description = "결제 완료 후 외부 시스템에 노티를 발송합니다.")
    @PostMapping("/send")
    public ResponseEntity<NotiResponse> sendNotification(@Valid @RequestBody NotiRequests.SendReq request) {
        log.info("[Noti] 노티 발송 요청: merchantId={}, orderId={}", request.merchantId(), request.orderId());

        NotiResponse response = notiService.processAndSendNoti(request);
        return ResponseEntity.ok(response);
    }
}