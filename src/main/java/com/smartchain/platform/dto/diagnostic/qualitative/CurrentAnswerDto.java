package com.smartchain.platform.dto.diagnostic.qualitative;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CurrentAnswerDto {
    private String value;
    private LocalDateTime answeredAt;
}
