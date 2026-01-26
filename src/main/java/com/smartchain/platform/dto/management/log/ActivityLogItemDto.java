package com.smartchain.platform.dto.management.log;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ActivityLogItemDto {
    private Long logId;
    private LocalDateTime timestamp;
    private String timestampLabel;
    private UserLogInfoDto user;         // v3.0: 사용자 정보 구조화
    private ActionInfoDto action;        // v3.0: 액션 정보 구조화
    private TargetInfoDto target;        // v3.0: 대상 정보 구조화
    private LogMetadataDto metadata;     // v3.0: 메타데이터 구조화
}
