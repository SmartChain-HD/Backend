package com.smartchain.platform.dto.diagnostic.upload;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DataStatusItemDto {
    private String dataName;             // "사업자 등록증"
    private boolean uploaded;            // 업로드 여부
    private String statusClass;          // CSS 클래스용 (uploaded/pending)
}
