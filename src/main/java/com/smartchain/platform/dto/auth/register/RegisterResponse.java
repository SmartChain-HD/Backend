package com.smartchain.platform.dto.auth.register;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RegisterResponse {
    private Long userId;
    private String email;
    private String name;
    private String role;                 // v3.0: 항상 "GUEST"로 시작
    private String message;              // "회원가입이 완료되었습니다"
    private String nextStep;             // "ROLE_REQUEST" - 다음 단계 안내
}
