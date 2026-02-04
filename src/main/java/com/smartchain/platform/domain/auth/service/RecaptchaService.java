package com.smartchain.platform.domain.auth.service;

import com.smartchain.platform.global.config.RecaptchaConfig;
import com.smartchain.platform.global.error.CustomException;
import com.smartchain.platform.global.error.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;

import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class RecaptchaService {

    private static final String VERIFY_URL = "https://www.google.com/recaptcha/api/siteverify";

    private final RecaptchaConfig recaptchaConfig;
    private final RestClient restClient = RestClient.create();

    /**
     * reCAPTCHA v3 토큰 검증
     * @param token 프론트엔드에서 전달받은 reCAPTCHA 토큰
     * @param expectedAction 예상 action (예: "login")
     */
    public void verify(String token, String expectedAction) {
        if (!recaptchaConfig.isEnabled()) {
            log.debug("reCAPTCHA verification is disabled");
            return;
        }

        if (token == null || token.isBlank()) {
            log.warn("reCAPTCHA token is missing");
            throw new CustomException(ErrorCode.RECAPTCHA_FAILED);
        }

        try {
            MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
            formData.add("secret", recaptchaConfig.getSecretKey());
            formData.add("response", token);

            @SuppressWarnings("unchecked")
            Map<String, Object> response = restClient.post()
                    .uri(VERIFY_URL)
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .body(formData)
                    .retrieve()
                    .body(Map.class);

            if (response == null) {
                log.error("reCAPTCHA API returned null response");
                throw new CustomException(ErrorCode.RECAPTCHA_FAILED);
            }

            log.debug("reCAPTCHA response: {}", response);

            // success 검증
            Boolean success = (Boolean) response.get("success");
            if (!Boolean.TRUE.equals(success)) {
                log.warn("reCAPTCHA verification failed: success=false, error-codes={}",
                        response.get("error-codes"));
                throw new CustomException(ErrorCode.RECAPTCHA_FAILED);
            }

            // action 검증 (변조 방지)
            String action = (String) response.get("action");
            if (expectedAction != null && !expectedAction.equals(action)) {
                log.warn("reCAPTCHA action mismatch: expected={}, actual={}", expectedAction, action);
                throw new CustomException(ErrorCode.RECAPTCHA_FAILED);
            }

            // score 검증
            Double score = ((Number) response.get("score")).doubleValue();
            if (score < recaptchaConfig.getScoreThreshold()) {
                log.warn("reCAPTCHA low score: score={}, threshold={}",
                        score, recaptchaConfig.getScoreThreshold());
                throw new CustomException(ErrorCode.RECAPTCHA_LOW_SCORE);
            }

            log.info("reCAPTCHA verification passed: score={}, action={}", score, action);

        } catch (CustomException e) {
            throw e;
        } catch (Exception e) {
            log.error("reCAPTCHA verification error", e);
            throw new CustomException(ErrorCode.RECAPTCHA_FAILED);
        }
    }
}
