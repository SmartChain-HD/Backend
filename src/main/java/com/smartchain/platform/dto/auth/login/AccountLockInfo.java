package com.smartchain.platform.dto.auth.login;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AccountLockInfo {
    private LocalDateTime lockedUntil;
    private Long remainingMinutes;
    private Integer remainingAttempts;
    private String warningMessage;
}
