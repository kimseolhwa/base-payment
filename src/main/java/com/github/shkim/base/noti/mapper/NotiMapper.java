package com.github.shkim.base.noti.mapper;

import com.github.shkim.base.noti.dto.NotiRequests;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface NotiMapper {
    int insertNotiHistory(NotiRequests.SendReq request);
}