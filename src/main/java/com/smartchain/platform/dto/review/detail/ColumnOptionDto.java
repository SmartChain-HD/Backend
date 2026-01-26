package com.smartchain.platform.dto.review.detail;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ColumnOptionDto {
    private String columnCode;           // DATE, MODIFIER, FIELD, STATUS, PREV_VALUE, NEW_VALUE, REASON
    private String columnLabel;          // "기간", "수정자", "항목"...
    private boolean selected;
}
