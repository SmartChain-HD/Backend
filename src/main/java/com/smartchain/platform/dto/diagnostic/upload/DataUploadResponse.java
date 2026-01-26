package com.smartchain.platform.dto.diagnostic.upload;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DataUploadResponse {
    private Long slotId;
    private UploadedFileInfoDto uploadedFile;
    private String parsingStatus;        // PENDING, PROCESSING, SUCCESS, FAILED
    private String message;
    private int progressRate;            // 업데이트된 진행률
}
