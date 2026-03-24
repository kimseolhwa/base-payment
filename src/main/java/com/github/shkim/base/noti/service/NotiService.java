package com.github.shkim.base.noti.service;

import com.github.shkim.base.noti.dto.NotiRequests;
import com.github.shkim.base.noti.dto.NotiResponse;
import com.github.shkim.base.noti.mapper.NotiMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotiService {

    private final NotiMapper notiMapper;
    private final ExternalNotiClient externalNotiClient; // 분리된 클라이언트 주입

    /**
     * DB에 발송 이력을 적재하고, 서킷 브레이커를 통해 외부 통신 수행.
     */
    @Transactional
    public NotiResponse processAndSendNoti(NotiRequests.SendReq request) {
        // 1. DB에 노티 발송 시도 이력 적재
        saveNotiHistory(request);

        // 2. 외부 API 통신 (서킷 브레이커 작동)
        String externalResult = externalNotiClient.callExternalApi(request);

        return NotiResponse.builder()
                .resCd("0000")
                .resMsg("노티 처리가 완료되었습니다. (결과: " + externalResult + ")")
                .build();
    }

    @Transactional // DB 작업에만 트랜잭션을 적용
    public void saveNotiHistory(NotiRequests.SendReq req) {
        int result = notiMapper.insertNotiHistory(req);
        if (result <= 0) {
            throw new RuntimeException("노티 이력 적재 실패");
        }
    }
}