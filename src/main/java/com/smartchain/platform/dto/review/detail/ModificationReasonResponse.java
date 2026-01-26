package com.smartchain.platform.dto.review.detail;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ModificationReasonResponse {
    private Long logId;
    private String field;
    private String previousValue;
    private String newValue;
    private String reason;
    private String modifierName;
    private LocalDateTime modifiedAt;
}
