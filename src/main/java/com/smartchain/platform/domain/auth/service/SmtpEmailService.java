package com.smartchain.platform.domain.auth.service;

import com.smartchain.platform.global.error.CustomException;
import com.smartchain.platform.global.error.ErrorCode;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

/**
 * SMTP 기반 이메일 발송 서비스.
 * dev 프로파일에서 활성화됩니다.
 */
@Slf4j
@Service
@Profile("dev")
@RequiredArgsConstructor
public class SmtpEmailService implements EmailService {

    private final JavaMailSender mailSender;

    private static final String SENDER_NAME = "SmartChain";
    private static final String SUBJECT = "[SmartChain] 이메일 인증 코드";

    @Override
    public void sendVerificationCode(String to, String code, int expiryMinutes) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(to);
            helper.setSubject(SUBJECT);
            helper.setText(buildEmailContent(code, expiryMinutes), true);

            mailSender.send(message);
            log.info("Verification email sent successfully: to={}", to);
        } catch (MessagingException e) {
            log.error("Failed to send verification email: to={}, error={}", to, e.getMessage());
            throw new CustomException(ErrorCode.EMAIL_SEND_FAILED);
        }
    }

    private String buildEmailContent(String code, int expiryMinutes) {
        return String.format("""
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <style>
                    body { font-family: 'Noto Sans KR', Arial, sans-serif; }
                    .container { max-width: 600px; margin: 0 auto; padding: 20px; }
                    .code { font-size: 32px; font-weight: bold; color: #2563eb; letter-spacing: 8px; }
                    .notice { color: #666; font-size: 14px; margin-top: 20px; }
                </style>
            </head>
            <body>
                <div class="container">
                    <h2>SmartChain 이메일 인증</h2>
                    <p>아래 인증 코드를 입력해 주세요.</p>
                    <p class="code">%s</p>
                    <p class="notice">
                        이 인증 코드는 %d분 후에 만료됩니다.<br>
                        본인이 요청하지 않은 경우 이 메일을 무시해 주세요.
                    </p>
                </div>
            </body>
            </html>
            """, code, expiryMinutes);
    }
}
