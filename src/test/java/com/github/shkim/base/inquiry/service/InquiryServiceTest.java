package com.github.shkim.base.inquiry.service;

import com.github.shkim.base.inquiry.dto.InquiryRequests;
import com.github.shkim.base.inquiry.dto.InquiryResponse;
import com.github.shkim.base.inquiry.mapper.InquiryMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class InquiryServiceTest {

    @Mock
    private InquiryMapper inquiryMapper;

    @InjectMocks
    private InquiryService inquiryService;

    @Test
    @DisplayName("결제 내역 조회 성공 테스트 - DB에서 데이터를 정상적으로 가져와 응답 객체로 변환한다.")
    void getPaymentDetail_Success() {
        // given
        InquiryRequests.PaymentDetailReq req = new InquiryRequests.PaymentDetailReq("TEST_M001", "ORD001");

        Map<String, Object> mockDbResult = new HashMap<>();
        mockDbResult.put("AMOUNT", 50000);
        mockDbResult.put("STATUS", "SUCCESS");

        // 매퍼가 요청 DTO 객체를 파라미터로 받아 호출될 때, 준비된 Mock 데이터를 반환하도록 설정
        given(inquiryMapper.selectPaymentDetail(any(InquiryRequests.PaymentDetailReq.class))).willReturn(mockDbResult);

        // when
        InquiryResponse response = inquiryService.getPaymentDetail(req);

        // then
        assertThat(response.resCd()).isEqualTo("0000");

        // 응답 객체의 data 필드가 최상위 Object 타입이므로, 내부 상태를 검증하기 위해 Map으로 안전하게 형변환(Casting)
        @SuppressWarnings("unchecked")
        Map<String, Object> responseData = (Map<String, Object>) response.data();
        assertThat(responseData.get("STATUS")).isEqualTo("SUCCESS");

        // 매퍼의 조회 메서드가 해당 요청 DTO 객체와 함께 정확히 1번 호출되었는지 행위 검증
        verify(inquiryMapper).selectPaymentDetail(req);
    }
}