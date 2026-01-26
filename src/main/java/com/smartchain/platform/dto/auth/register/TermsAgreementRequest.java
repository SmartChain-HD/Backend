package com.smartchain.platform.dto.auth.register;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TermsAgreementRequest {
    
    @NotNull(message = "개인정보 수집이용 동의는 필수입니다")
    private Boolean privacyPolicyAgreed;         // [필수]
    
    @NotNull(message = "서비스 이용약관 동의는 필수입니다")
    private Boolean serviceTermsAgreed;          // [필수]
    
    private Boolean marketingAgreed;             // [선택]
    
    private List<TermsAgreementItemDto> additionalTerms;  // 추가 약관
}
