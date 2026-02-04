package com.smartchain.platform.dto.auth.login;

import com.smartchain.platform.dto.auth.common.UserInfoDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponse {
    private String accessToken;
    private String refreshToken;
    private String tokenType;            // v3.0: "Bearer" 추가
    private Long expiresIn;              // 토큰 만료 시간 (초)
    private UserInfoDto user;

    // 로그인 실패 경고 (선택)
    private Integer remainingAttempts;   // 남은 시도 횟수
    private String warningMessage;       // 경고 메시지
}
