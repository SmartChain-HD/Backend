package com.smartchain.platform.dto.auth.register;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TermsItemDto {
    private Long termsId;
    private String termsCode;            // PRIVACY, SERVICE, MARKETING
    private String termsTitle;           // "[필수] 0000 수집이용"
    private String termsContent;         // 약관 내용 전문
    private boolean required;            // 필수 여부
    private Integer displayOrder;
}
