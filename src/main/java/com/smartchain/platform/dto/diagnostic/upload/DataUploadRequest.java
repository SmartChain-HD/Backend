package com.smartchain.platform.dto.diagnostic.upload;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DataUploadRequest {
    @NotNull
    private Long diagnosticId;
    
    @NotNull
    private Long slotId;
    
    private String unit;                 // 단위 (필요시)
    
    // 파일은 MultipartFile로 별도 처리
}
