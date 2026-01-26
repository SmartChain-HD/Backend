package com.smartchain.platform.dto.auth.logout;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LogoutRequest {
    private String refreshToken;         // 리프레시 토큰 무효화용
}
