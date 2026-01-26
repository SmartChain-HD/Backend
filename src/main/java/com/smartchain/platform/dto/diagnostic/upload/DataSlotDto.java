package com.smartchain.platform.dto.diagnostic.upload;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DataSlotDto {
    private Long slotId;
    private String slotCode;             // "BUSINESS_LICENSE", "GAS_BILL"
    private String dataName;             // "사업자 등록증"
    private String acceptedFormats;      // "docx/pdf"
    private boolean isRequired;
    private UploadedFileInfoDto uploadedFile;    // 업로드된 파일 정보
    private String selectedUnit;         // 선택된 단위 "KW"
    private List<String> availableUnits; // 선택 가능 단위
    private String uploadStatus;         // EMPTY, UPLOADING, SUCCESS, FAILED
    private String errorMessage;         // 업로드 실패시 원인
}
