package com.smartchain.platform.domain.auth.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Slf4j
@Service // 이 어노테이션이 있어야 AuthService에 주입될 수 있습니다.
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;

    @Override
    public void sendVerificationCode(String to, String code, int expiryMinutes) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(to);
            message.setSubject("[SmartChain] 이메일 인증 번호 안내");
            message.setText(String.format(
                "안녕하세요.\n\n인증 번호는 [%s] 입니다.\n입력 제한 시간은 %d분입니다.\n\n감사합니다.",
                code, expiryMinutes
            ));
            
            mailSender.send(message);
            log.info("인증 이메일 발송 완료: to={}, code={}", to, code);
        } catch (Exception e) {
            // 이메일 발송 실패가 전체 서비스 중단으로 이어지지 않도록 로그만 남깁니다.
            log.error("이메일 발송 실패: {}", e.getMessage());
        }
    }
}