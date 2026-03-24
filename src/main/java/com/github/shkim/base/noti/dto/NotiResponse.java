package com.github.shkim.base.noti.dto;

import lombok.Builder;

@Builder
public record NotiResponse(String resCd, String resMsg) {}