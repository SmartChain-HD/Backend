package com.smartchain.platform.dto.ai.run;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

/**
 * AI Run API Preview 요청 DTO
 * POST /run/preview
 */
public record RunPreviewRequest(
    String domain,  // safety, compliance, esg

    @JsonProperty("period_start")
    String periodStart,

    @JsonProperty("period_end")
    String periodEnd,

    @JsonProperty("package_id")
    String packageId,  // 첫 호출 시 null

    @JsonProperty("added_files")
    List<FileInfo> addedFiles,

    @JsonProperty("removed_file_ids")
    List<String> removedFileIds
) {}
