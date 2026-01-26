package com.smartchain.platform.dto.campaign.dashboard;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardTaskDto {
    private String taskType;             // DRAFT, APPROVAL, REVIEW, PERMISSION_APPROVAL
    private String taskTitle;
    private String taskDescription;
    private String linkUrl;
    private LocalDate dueDate;
    private Integer dDay;
    private String priority;             // HIGH, MEDIUM, LOW
}
