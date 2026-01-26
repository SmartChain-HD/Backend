package com.smartchain.platform.dto.auth.email;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SendVerificationResponse {
    private String email;
    private String message;
    private int expiresInSeconds;
}
