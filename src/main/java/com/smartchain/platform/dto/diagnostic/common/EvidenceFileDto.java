package com.smartchain.platform.dto.diagnostic.common;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EvidenceFileDto {
    private Long fileId;
    private String fileName;
    private String fileType;
}
