package com.smartchain.platform.domain.auth.entity;

import com.smartchain.platform.global.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "email_verification_code")
public class EmailVerificationCode extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String email;

    @Column(nullable = false, length = 6)
    private String code;

    @Column(nullable = false)
    private LocalDateTime expiresAt;

    @Column(nullable = false)
    private boolean verified;

    @Builder
    public EmailVerificationCode(String email, String code, LocalDateTime expiresAt) {
        this.email = email;
        this.code = code;
        this.expiresAt = expiresAt;
        this.verified = false;
    }

    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiresAt);
    }

    public boolean isValid(String inputCode) {
        return !isExpired() && !verified && this.code.equals(inputCode);
    }

    public void markAsVerified() {
        this.verified = true;
    }
}
