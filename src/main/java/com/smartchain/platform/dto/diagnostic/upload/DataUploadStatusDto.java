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
public class DataUploadStatusDto {
    private int requiredTotal;           // 필수 데이터 총 개수
    private int requiredUploaded;        // 필수 데이터 업로드 완료 수
    private int optionalTotal;           // 옵션 데이터 총 개수
    private int optionalUploaded;        // 옵션 데이터 업로드 완료 수
    private List<DataStatusItemDto> requiredItems;
    private List<DataStatusItemDto> optionalItems;
}
