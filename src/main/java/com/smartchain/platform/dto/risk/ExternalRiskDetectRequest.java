package com.smartchain.platform.dto.risk;

import java.util.List;

/**
 * Backend -> AI 서버 요청 DTO
 * POST /risk/external/detect
 */
public record ExternalRiskDetectRequest(
    List<String> vendors,
    boolean rag
) {
    public static ExternalRiskDetectRequest of(List<String> vendorNames) {
        return new ExternalRiskDetectRequest(vendorNames, true);
    }
}
