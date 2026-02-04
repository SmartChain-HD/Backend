package com.smartchain.platform.domain.auth.repository;

import com.smartchain.platform.domain.auth.entity.EmailVerificationCode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface EmailVerificationCodeRepository extends JpaRepository<EmailVerificationCode, Long> {

    Optional<EmailVerificationCode> findTopByEmailAndVerifiedFalseOrderByCreatedAtDesc(String email);

    Optional<EmailVerificationCode> findTopByEmailAndVerifiedTrueOrderByCreatedAtDesc(String email);

    Optional<EmailVerificationCode> findTopByEmailOrderByCreatedAtDesc(String email);

    void deleteByExpiresAtBefore(LocalDateTime dateTime);
}
