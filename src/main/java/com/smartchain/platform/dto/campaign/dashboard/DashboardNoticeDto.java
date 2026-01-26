package com.smartchain.platform.dto.campaign.dashboard;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardNoticeDto {
    private Long noticeId;
    private String title;
    private String content;
    private boolean isImportant;
    private LocalDateTime createdAt;
    private String createdAtLabel;
}
