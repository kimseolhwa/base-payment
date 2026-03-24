package com.github.shkim.base.noti.service;

import com.github.shkim.base.noti.dto.NotiRequests;
import com.github.shkim.base.noti.dto.NotiResponse;
import com.github.shkim.base.noti.mapper.NotiMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class NotiServiceTest {

    @Mock
    private NotiMapper notiMapper; // 가짜 DB 매퍼

    @Mock
    private ExternalNotiClient externalNotiClient; // 가짜 외부 통신 클라이언트

    @InjectMocks
    private NotiService notiService; // 테스트 대상

    @Test
    @DisplayName("노티 발송 성공 테스트 - 이력 적재 후 외부 통신 결과를 포함하여 반환한다.")
    void processAndSendNoti_Success() {
        // given
        NotiRequests.SendReq req = new NotiRequests.SendReq("TEST_M001", "ORD001", new BigDecimal("1000"), "SUCCESS");

        // DB Insert 성공(1) 반환 조작
        given(notiMapper.insertNotiHistory(any(NotiRequests.SendReq.class))).willReturn(1);
        // 외부 통신 결과 문자열 반환 조작
        given(externalNotiClient.callExternalApi(any(NotiRequests.SendReq.class))).willReturn("EXTERNAL_API_SUCCESS");

        // when
        NotiResponse response = notiService.processAndSendNoti(req);

        // then
        assertThat(response.resCd()).isEqualTo("0000");
        assertThat(response.resMsg()).contains("EXTERNAL_API_SUCCESS"); // 응답 메시지에 결과가 포함되었는지 확인

        // 순서대로 각각 1번씩 호출되었는지 검증
        verify(notiMapper).insertNotiHistory(req);
        verify(externalNotiClient).callExternalApi(req);
    }
}