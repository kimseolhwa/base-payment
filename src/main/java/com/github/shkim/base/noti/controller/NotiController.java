package com.github.shkim.base.noti.controller;

import com.github.shkim.base.noti.service.NotiService;
import com.github.shkim.base.noti.dto.NotiRequests;
import com.github.shkim.base.noti.dto.NotiResponse;
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
@Slf4j
@RestController
@RequestMapping("/api/v1/noti")
@RequiredArgsConstructor
public class NotiController {

    private final NotiService notiService;

    @PostMapping("/send")
    public ResponseEntity<NotiResponse> sendNotification(@Valid @RequestBody NotiRequests.SendReq request) {
        log.info("[Noti] 노티 발송 요청: merchantId={}, orderId={}", request.merchantId(), request.orderId());

        NotiResponse response = notiService.processAndSendNoti(request);
        return ResponseEntity.ok(response);
    }
}