package com.smartchain.platform.dto.ai.run;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * AI Run API 파일 정보 DTO
 */
public record FileInfo(
    @JsonProperty("file_id")
    String fileId,

    @JsonProperty("storage_uri")
    String storageUri,

    @JsonProperty("file_name")
    String fileName
) {
    public FileInfo(String fileId, String storageUri) {
        this(fileId, storageUri, null);
    }
}
