package com.smartchain.platform.dto.common.file;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FileBulkDeleteResponse {
    private List<Long> deletedFileIds;
    private int deletedCount;
    private String message;
    private LocalDateTime deletedAt;
}
