package com.smartchain.platform.dto.approval.tab;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DiagnosticPdfTabResponse {
    private String pdfUrl;               // PDF 뷰어 URL
    private int totalPages;
    private String downloadUrl;
}
