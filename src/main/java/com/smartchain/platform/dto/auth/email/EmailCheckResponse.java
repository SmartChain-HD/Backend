package com.smartchain.platform.dto.auth.email;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmailCheckResponse {
    private String email;                // v3.0: email 필드 추가
    private boolean available;           // 사용 가능 여부
    private String message;              // "사용 가능한 이메일입니다" / "이미 사용중인 이메일입니다"
}
