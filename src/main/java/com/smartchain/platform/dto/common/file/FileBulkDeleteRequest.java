package com.smartchain.platform.dto.common.file;

import java.util.List;

/**
 * 파일 다건 삭제 요청 DTO
 */
public record FileBulkDeleteRequest(
    List<Long> fileIds
) {}
