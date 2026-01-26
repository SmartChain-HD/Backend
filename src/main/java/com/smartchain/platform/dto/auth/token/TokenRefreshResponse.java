package com.smartchain.platform.dto.auth.token;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TokenRefreshResponse {
    private String accessToken;
    private String refreshToken;
    private String tokenType;            // v3.0: "Bearer" 추가
    private Long expiresIn;
}
