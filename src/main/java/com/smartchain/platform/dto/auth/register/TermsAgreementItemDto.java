package com.smartchain.platform.dto.auth.register;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TermsAgreementItemDto {
    private Long termsId;
    private Boolean agreed;
}
