package com.smartchain.platform.dto.approval.download;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BulkDownloadRequest {
    private List<Long> approvalIds;      // v3.0: auditIds → approvalIds
}
