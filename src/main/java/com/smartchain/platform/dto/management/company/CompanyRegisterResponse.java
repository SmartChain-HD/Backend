package com.smartchain.platform.dto.management.company;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CompanyRegisterResponse {
    private Long companyId;
    private String companyName;
    private String businessNumber;
    private String status;
    private LocalDateTime createdAt;
    private String message;
}
