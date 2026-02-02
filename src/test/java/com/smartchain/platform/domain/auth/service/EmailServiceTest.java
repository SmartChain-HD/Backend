package com.smartchain.platform.domain.auth.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThatCode;

@ExtendWith(MockitoExtension.class)
class EmailServiceTest {

    @Test
    @DisplayName("LocalEmailService: 인증 코드 발송 시 예외 없이 로깅")
    void localEmailService_sendVerificationCode_Success() {
        // given
        EmailService emailService = new LocalEmailService();
        String email = "test@test.com";
        String code = "123456";
        int expiryMinutes = 5;

        // when & then
        assertThatCode(() -> emailService.sendVerificationCode(email, code, expiryMinutes))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("LocalEmailService: 다양한 이메일 주소 처리")
    void localEmailService_variousEmails_Success() {
        // given
        EmailService emailService = new LocalEmailService();

        // when & then
        assertThatCode(() -> {
            emailService.sendVerificationCode("user@gmail.com", "111111", 5);
            emailService.sendVerificationCode("admin@company.co.kr", "222222", 10);
            emailService.sendVerificationCode("test+alias@domain.org", "333333", 3);
        }).doesNotThrowAnyException();
    }
}
