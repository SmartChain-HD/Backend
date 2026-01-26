package com.smartchain.platform.dto.management.log;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ActionInfoDto {
    private String code;                 // LOGIN, UPLOAD, SUBMIT
    private String name;                 // 로그인, 파일 업로드, 제출
}
