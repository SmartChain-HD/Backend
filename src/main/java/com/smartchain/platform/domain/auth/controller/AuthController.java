package com.smartchain.platform.domain.auth.controller;

import com.smartchain.platform.domain.auth.service.AuthService;
import com.smartchain.platform.dto.auth.email.*;
import com.smartchain.platform.dto.auth.login.LoginRequest;
import com.smartchain.platform.dto.auth.login.LoginResponse;
import com.smartchain.platform.dto.auth.logout.LogoutRequest;
import com.smartchain.platform.dto.auth.myinfo.MyInfoResponse;
import com.smartchain.platform.dto.auth.register.RegisterRequest;
import com.smartchain.platform.dto.auth.register.RegisterResponse;
import com.smartchain.platform.dto.auth.token.TokenRefreshRequest;
import com.smartchain.platform.dto.auth.token.TokenRefreshResponse;
import com.smartchain.platform.global.response.BaseResponse;
import com.smartchain.platform.global.security.JwtTokenProvider;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "Auth", description = "인증 API")
public class AuthController {

    private final AuthService authService;
    private final JwtTokenProvider jwtTokenProvider;

    @Operation(summary = "회원가입", description = "새로운 사용자를 등록합니다. 가입 후 GUEST 역할이 부여됩니다.")
    @PostMapping("/register")
    public ResponseEntity<BaseResponse<RegisterResponse>> register(
            @Valid @RequestBody RegisterRequest request) {
        RegisterResponse response = authService.register(request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(BaseResponse.success("회원가입이 완료되었습니다", response));
    }

    @Operation(summary = "로그인", description = "이메일과 비밀번호로 로그인합니다.")
    @PostMapping("/login")
    public ResponseEntity<BaseResponse<LoginResponse>> login(
            @Valid @RequestBody LoginRequest request) {
        LoginResponse response = authService.login(request);
        return ResponseEntity.ok(BaseResponse.success("로그인 성공", response));
    }

    @Operation(summary = "이메일 중복 확인", description = "이메일 사용 가능 여부를 확인합니다.")
    @PostMapping("/check-email")
    public ResponseEntity<BaseResponse<EmailCheckResponse>> checkEmail(
            @Valid @RequestBody EmailCheckRequest request) {
        EmailCheckResponse response = authService.checkEmail(request);
        return ResponseEntity.ok(BaseResponse.success(response));
    }

    @Operation(summary = "이메일 인증 코드 발송", description = "이메일로 6자리 인증 코드를 발송합니다. 유효시간 5분, 재발송 제한 1분.")
    @PostMapping("/send-verification")
    public ResponseEntity<BaseResponse<SendVerificationResponse>> sendVerification(
            @Valid @RequestBody SendVerificationRequest request) {
        SendVerificationResponse response = authService.sendVerificationCode(request);
        return ResponseEntity.ok(BaseResponse.success(response));
    }

    @Operation(summary = "이메일 인증 확인", description = "발송된 인증 코드를 검증합니다.")
    @PostMapping("/verify-email")
    public ResponseEntity<BaseResponse<EmailVerificationResponse>> verifyEmail(
            @Valid @RequestBody EmailVerificationRequest request) {
        EmailVerificationResponse response = authService.verifyEmail(request);
        return ResponseEntity.ok(BaseResponse.success(response));
    }

    @Operation(summary = "토큰 갱신", description = "리프레시 토큰으로 새로운 액세스 토큰을 발급받습니다.")
    @PostMapping("/refresh")
    public ResponseEntity<BaseResponse<TokenRefreshResponse>> refreshToken(
            @Valid @RequestBody TokenRefreshRequest request) {
        TokenRefreshResponse response = authService.refreshToken(request);
        return ResponseEntity.ok(BaseResponse.success(response));
    }

    @Operation(summary = "로그아웃", description = "로그아웃 처리합니다. 클라이언트에서 토큰을 삭제해야 합니다.")
    @PostMapping("/logout")
    public ResponseEntity<BaseResponse<Void>> logout(
            @RequestBody(required = false) LogoutRequest request) {
        authService.logout(request);
        return ResponseEntity.ok(BaseResponse.success("로그아웃되었습니다"));
    }

    @Operation(summary = "내 정보 조회", description = "현재 로그인된 사용자의 정보를 조회합니다.")
    @GetMapping("/me")
    public ResponseEntity<BaseResponse<MyInfoResponse>> getMyInfo(HttpServletRequest request) {
        Long userId = extractUserIdFromRequest(request);
        MyInfoResponse response = authService.getMyInfo(userId);
        return ResponseEntity.ok(BaseResponse.success(response));
    }

    private Long extractUserIdFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            String token = bearerToken.substring(7);
            return jwtTokenProvider.getUserIdFromToken(token);
        }
        throw new IllegalArgumentException("Authorization header is missing or invalid");
    }
}
