package com.smartchain.platform.domain.auth.service;

import com.smartchain.platform.domain.auth.entity.EmailVerificationCode;
import com.smartchain.platform.domain.auth.repository.EmailVerificationCodeRepository;
import com.smartchain.platform.domain.role.repository.RoleRequestRepository;
import com.smartchain.platform.domain.user.entity.Role;
import com.smartchain.platform.domain.user.entity.User;
import com.smartchain.platform.domain.user.entity.UserDomainRole;
import com.smartchain.platform.domain.user.entity.Domain;
import com.smartchain.platform.domain.user.repository.RoleRepository;
import com.smartchain.platform.domain.user.repository.UserDomainRoleRepository;
import com.smartchain.platform.domain.user.repository.UserRepository;
import com.smartchain.platform.dto.auth.email.*;
import com.smartchain.platform.dto.auth.login.LoginRequest;
import com.smartchain.platform.dto.auth.login.LoginResponse;
import com.smartchain.platform.dto.auth.myinfo.MyDomainResponse;
import com.smartchain.platform.dto.auth.myinfo.MyInfoResponse;
import com.smartchain.platform.dto.auth.register.RegisterRequest;
import com.smartchain.platform.dto.auth.register.RegisterResponse;
import com.smartchain.platform.dto.auth.register.TermsAgreementRequest;
import com.smartchain.platform.dto.auth.token.TokenRefreshRequest;
import com.smartchain.platform.dto.auth.token.TokenRefreshResponse;
import com.smartchain.platform.global.enums.RequestStatus;
import com.smartchain.platform.global.enums.UserStatus;
import com.smartchain.platform.global.error.CustomException;
import com.smartchain.platform.global.error.ErrorCode;
import com.smartchain.platform.global.security.JwtTokenProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @InjectMocks
    private AuthService authService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private UserDomainRoleRepository userDomainRoleRepository;

    @Mock
    private RoleRequestRepository roleRequestRepository;

    @Mock
    private EmailVerificationCodeRepository verificationCodeRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @Mock
    private EmailService emailService;

    @Mock
    private RecaptchaService recaptchaService;

    private Role guestRole;

    @BeforeEach
    void setUp() {
        guestRole = new Role("게스트", "GUEST");
    }

    @Nested
    @DisplayName("회원가입 테스트")
    class RegisterTest {

        @Test
        @DisplayName("정상적인 회원가입 성공")
        void register_Success() {
            // given
            RegisterRequest request = RegisterRequest.builder()
                    .name("홍길동")
                    .email("test@test.com")
                    .password("Password123!")
                    .passwordConfirm("Password123!")
                    .terms(TermsAgreementRequest.builder()
                            .privacyPolicyAgreed(true)
                            .serviceTermsAgreed(true)
                            .marketingAgreed(false)
                            .build())
                    .build();

            given(userRepository.existsByEmail(anyString())).willReturn(false);
            given(roleRepository.findByCode("GUEST")).willReturn(Optional.of(guestRole));
            given(passwordEncoder.encode(anyString())).willReturn("encodedPassword");
            given(userRepository.save(any(User.class))).willAnswer(invocation -> {
                User user = invocation.getArgument(0);
                // Simulate ID generation
                return User.builder()
                        .name(user.getName())
                        .email(user.getEmail())
                        .userPassword(user.getUserPassword())
                        .role(user.getRole())
                        .build();
            });

            // when
            RegisterResponse response = authService.register(request);

            // then
            assertThat(response).isNotNull();
            assertThat(response.getEmail()).isEqualTo("test@test.com");
            assertThat(response.getName()).isEqualTo("홍길동");
            assertThat(response.getRole()).isEqualTo("GUEST");
            assertThat(response.getNextStep()).isEqualTo("ROLE_REQUEST");

            verify(userRepository).save(any(User.class));
        }

        @Test
        @DisplayName("비밀번호 불일치 시 예외 발생")
        void register_PasswordMismatch_ThrowsException() {
            // given
            RegisterRequest request = RegisterRequest.builder()
                    .name("홍길동")
                    .email("test@test.com")
                    .password("Password123!")
                    .passwordConfirm("DifferentPassword123!")
                    .terms(TermsAgreementRequest.builder()
                            .privacyPolicyAgreed(true)
                            .serviceTermsAgreed(true)
                            .build())
                    .build();

            // when & then
            assertThatThrownBy(() -> authService.register(request))
                    .isInstanceOf(CustomException.class)
                    .satisfies(ex -> {
                        CustomException ce = (CustomException) ex;
                        assertThat(ce.getErrorCode()).isEqualTo(ErrorCode.PASSWORD_MISMATCH);
                    });
        }

        @Test
        @DisplayName("비밀번호 형식 불일치 시 예외 발생")
        void register_InvalidPasswordFormat_ThrowsException() {
            // given
            RegisterRequest request = RegisterRequest.builder()
                    .name("홍길동")
                    .email("test@test.com")
                    .password("simple")  // 형식 불일치
                    .passwordConfirm("simple")
                    .terms(TermsAgreementRequest.builder()
                            .privacyPolicyAgreed(true)
                            .serviceTermsAgreed(true)
                            .build())
                    .build();

            // when & then
            assertThatThrownBy(() -> authService.register(request))
                    .isInstanceOf(CustomException.class)
                    .satisfies(ex -> {
                        CustomException ce = (CustomException) ex;
                        assertThat(ce.getErrorCode()).isEqualTo(ErrorCode.INVALID_PASSWORD_FORMAT);
                    });
        }

        @Test
        @DisplayName("이메일 중복 시 예외 발생")
        void register_DuplicateEmail_ThrowsException() {
            // given
            RegisterRequest request = RegisterRequest.builder()
                    .name("홍길동")
                    .email("existing@test.com")
                    .password("Password123!")
                    .passwordConfirm("Password123!")
                    .terms(TermsAgreementRequest.builder()
                            .privacyPolicyAgreed(true)
                            .serviceTermsAgreed(true)
                            .build())
                    .build();

            given(userRepository.existsByEmail("existing@test.com")).willReturn(true);

            // when & then
            assertThatThrownBy(() -> authService.register(request))
                    .isInstanceOf(CustomException.class)
                    .satisfies(ex -> {
                        CustomException ce = (CustomException) ex;
                        assertThat(ce.getErrorCode()).isEqualTo(ErrorCode.DUPLICATE_EMAIL);
                    });
        }
    }

    @Nested
    @DisplayName("로그인 테스트")
    class LoginTest {

        @Test
        @DisplayName("정상적인 로그인 성공")
        void login_Success() {
            // given
            LoginRequest request = LoginRequest.builder()
                    .email("test@test.com")
                    .password("Password123!")
                    .build();

            User user = User.builder()
                    .name("홍길동")
                    .email("test@test.com")
                    .userPassword("encodedPassword")
                    .role(guestRole)
                    .emailVerified(true)
                    .build();

            given(userRepository.findByEmail("test@test.com")).willReturn(Optional.of(user));
            given(passwordEncoder.matches("Password123!", "encodedPassword")).willReturn(true);
            given(jwtTokenProvider.createAccessToken(any(), anyString(), anyString())).willReturn("accessToken");
            given(jwtTokenProvider.createRefreshToken(any())).willReturn("refreshToken");
            given(jwtTokenProvider.getAccessTokenValidityInSeconds()).willReturn(3600L);
            given(userDomainRoleRepository.findByUserIdWithDomainAndRole(any())).willReturn(Collections.emptyList());

            // when
            LoginResponse response = authService.login(request);

            // then
            assertThat(response).isNotNull();
            assertThat(response.getAccessToken()).isEqualTo("accessToken");
            assertThat(response.getRefreshToken()).isEqualTo("refreshToken");
            assertThat(response.getTokenType()).isEqualTo("Bearer");
            assertThat(response.getUser().getEmail()).isEqualTo("test@test.com");
        }

        @Test
        @DisplayName("잘못된 비밀번호로 로그인 실패")
        void login_WrongPassword_ThrowsException() {
            // given
            LoginRequest request = LoginRequest.builder()
                    .email("test@test.com")
                    .password("WrongPassword123!")
                    .build();

            User user = User.builder()
                    .name("홍길동")
                    .email("test@test.com")
                    .userPassword("encodedPassword")
                    .role(guestRole)
                    .build();

            given(userRepository.findByEmail("test@test.com")).willReturn(Optional.of(user));
            given(passwordEncoder.matches("WrongPassword123!", "encodedPassword")).willReturn(false);

            // when & then
            assertThatThrownBy(() -> authService.login(request))
                    .isInstanceOf(CustomException.class)
                    .satisfies(ex -> {
                        CustomException ce = (CustomException) ex;
                        assertThat(ce.getErrorCode()).isEqualTo(ErrorCode.INVALID_CREDENTIALS);
                    });
        }

        @Test
        @DisplayName("비활성화된 계정 로그인 시 ACCOUNT_DISABLED")
        void login_DisabledAccount_ThrowsException() {
            // given
            LoginRequest request = LoginRequest.builder()
                    .email("test@test.com")
                    .password("Password123!")
                    .build();

            User user = User.builder()
                    .name("홍길동")
                    .email("test@test.com")
                    .userPassword("encodedPassword")
                    .role(guestRole)
                    .status(UserStatus.INACTIVE)
                    .build();

            given(userRepository.findByEmail("test@test.com")).willReturn(Optional.of(user));
            given(passwordEncoder.matches("Password123!", "encodedPassword")).willReturn(true);

            // when & then
            assertThatThrownBy(() -> authService.login(request))
                    .isInstanceOf(CustomException.class)
                    .satisfies(ex -> {
                        CustomException ce = (CustomException) ex;
                        assertThat(ce.getErrorCode()).isEqualTo(ErrorCode.ACCOUNT_DISABLED);
                    });
        }

        @Test
        @DisplayName("잠긴 계정 로그인 시 ACCOUNT_LOCKED")
        void login_LockedAccount_ThrowsException() {
            // given
            LoginRequest request = LoginRequest.builder()
                    .email("test@test.com")
                    .password("Password123!")
                    .build();

            User user = User.builder()
                    .name("홍길동")
                    .email("test@test.com")
                    .userPassword("encodedPassword")
                    .role(guestRole)
                    .locked(true)
                    .build();

            given(userRepository.findByEmail("test@test.com")).willReturn(Optional.of(user));
            given(passwordEncoder.matches("Password123!", "encodedPassword")).willReturn(true);

            // when & then
            assertThatThrownBy(() -> authService.login(request))
                    .isInstanceOf(CustomException.class)
                    .satisfies(ex -> {
                        CustomException ce = (CustomException) ex;
                        assertThat(ce.getErrorCode()).isEqualTo(ErrorCode.ACCOUNT_LOCKED);
                    });
        }

        @Test
        @DisplayName("이메일 미인증 계정 로그인 시 ACCOUNT_NOT_VERIFIED")
        void login_UnverifiedAccount_ThrowsException() {
            // given
            LoginRequest request = LoginRequest.builder()
                    .email("test@test.com")
                    .password("Password123!")
                    .build();

            User user = User.builder()
                    .name("홍길동")
                    .email("test@test.com")
                    .userPassword("encodedPassword")
                    .role(guestRole)
                    .emailVerified(false)
                    .build();

            given(userRepository.findByEmail("test@test.com")).willReturn(Optional.of(user));
            given(passwordEncoder.matches("Password123!", "encodedPassword")).willReturn(true);

            // when & then
            assertThatThrownBy(() -> authService.login(request))
                    .isInstanceOf(CustomException.class)
                    .satisfies(ex -> {
                        CustomException ce = (CustomException) ex;
                        assertThat(ce.getErrorCode()).isEqualTo(ErrorCode.ACCOUNT_NOT_VERIFIED);
                    });
        }
    }

    @Nested
    @DisplayName("이메일 중복 확인 테스트")
    class CheckEmailTest {

        @Test
        @DisplayName("사용 가능한 이메일")
        void checkEmail_Available() {
            // given
            EmailCheckRequest request = EmailCheckRequest.builder()
                    .email("new@test.com")
                    .build();

            given(userRepository.existsByEmail("new@test.com")).willReturn(false);

            // when
            EmailCheckResponse response = authService.checkEmail(request);

            // then
            assertThat(response.isAvailable()).isTrue();
            assertThat(response.getEmail()).isEqualTo("new@test.com");
        }

        @Test
        @DisplayName("이미 사용 중인 이메일")
        void checkEmail_NotAvailable() {
            // given
            EmailCheckRequest request = EmailCheckRequest.builder()
                    .email("existing@test.com")
                    .build();

            given(userRepository.existsByEmail("existing@test.com")).willReturn(true);

            // when
            EmailCheckResponse response = authService.checkEmail(request);

            // then
            assertThat(response.isAvailable()).isFalse();
        }
    }

    @Nested
    @DisplayName("이메일 인증 테스트")
    class EmailVerificationTest {

        @Test
        @DisplayName("인증 코드 발송 성공")
        void sendVerificationCode_Success() {
            // given
            SendVerificationRequest request = SendVerificationRequest.builder()
                    .email("test@test.com")
                    .build();

            given(verificationCodeRepository.findTopByEmailOrderByCreatedAtDesc("test@test.com"))
                    .willReturn(Optional.empty());
            given(verificationCodeRepository.save(any(EmailVerificationCode.class)))
                    .willAnswer(invocation -> invocation.getArgument(0));

            // when
            SendVerificationResponse response = authService.sendVerificationCode(request);

            // then
            assertThat(response.getEmail()).isEqualTo("test@test.com");
            assertThat(response.getExpiresInSeconds()).isEqualTo(300);
            verify(verificationCodeRepository).save(any(EmailVerificationCode.class));
            verify(emailService).sendVerificationCode(eq("test@test.com"), anyString(), eq(5));
        }

        @Test
        @DisplayName("인증 코드 검증 성공")
        void verifyEmail_Success() {
            // given
            EmailVerificationRequest request = EmailVerificationRequest.builder()
                    .email("test@test.com")
                    .verificationCode("123456")
                    .build();

            EmailVerificationCode verificationCode = EmailVerificationCode.builder()
                    .email("test@test.com")
                    .code("123456")
                    .expiresAt(LocalDateTime.now().plusMinutes(5))
                    .build();

            given(verificationCodeRepository.findTopByEmailAndVerifiedFalseOrderByCreatedAtDesc("test@test.com"))
                    .willReturn(Optional.of(verificationCode));

            // when
            EmailVerificationResponse response = authService.verifyEmail(request);

            // then
            assertThat(response.isVerified()).isTrue();
        }

        @Test
        @DisplayName("잘못된 인증 코드로 검증 실패")
        void verifyEmail_InvalidCode_ThrowsException() {
            // given
            EmailVerificationRequest request = EmailVerificationRequest.builder()
                    .email("test@test.com")
                    .verificationCode("000000")
                    .build();

            EmailVerificationCode verificationCode = EmailVerificationCode.builder()
                    .email("test@test.com")
                    .code("123456")
                    .expiresAt(LocalDateTime.now().plusMinutes(5))
                    .build();

            given(verificationCodeRepository.findTopByEmailAndVerifiedFalseOrderByCreatedAtDesc("test@test.com"))
                    .willReturn(Optional.of(verificationCode));

            // when & then
            assertThatThrownBy(() -> authService.verifyEmail(request))
                    .isInstanceOf(CustomException.class)
                    .satisfies(ex -> {
                        CustomException ce = (CustomException) ex;
                        assertThat(ce.getErrorCode()).isEqualTo(ErrorCode.INVALID_VERIFICATION_CODE);
                    });
        }
    }

    @Nested
    @DisplayName("토큰 갱신 테스트")
    class RefreshTokenTest {

        @Test
        @DisplayName("정상적인 토큰 갱신 성공")
        void refreshToken_Success() {
            // given
            TokenRefreshRequest request = TokenRefreshRequest.builder()
                    .refreshToken("validRefreshToken")
                    .build();

            User user = User.builder()
                    .name("홍길동")
                    .email("test@test.com")
                    .userPassword("encodedPassword")
                    .role(guestRole)
                    .build();

            given(jwtTokenProvider.validateTokenWithResult("validRefreshToken"))
                    .willReturn(JwtTokenProvider.TokenValidationResult.VALID);
            given(jwtTokenProvider.getUserIdFromToken("validRefreshToken")).willReturn(1L);
            given(userRepository.findById(1L)).willReturn(Optional.of(user));
            given(jwtTokenProvider.createAccessToken(any(), anyString(), anyString())).willReturn("newAccessToken");
            given(jwtTokenProvider.createRefreshToken(any())).willReturn("newRefreshToken");
            given(jwtTokenProvider.getAccessTokenValidityInSeconds()).willReturn(3600L);

            // when
            TokenRefreshResponse response = authService.refreshToken(request);

            // then
            assertThat(response).isNotNull();
            assertThat(response.getAccessToken()).isEqualTo("newAccessToken");
            assertThat(response.getRefreshToken()).isEqualTo("newRefreshToken");
            assertThat(response.getTokenType()).isEqualTo("Bearer");
            assertThat(response.getExpiresIn()).isEqualTo(3600L);
        }

        @Test
        @DisplayName("만료된 리프레시 토큰으로 갱신 실패")
        void refreshToken_ExpiredToken_ThrowsException() {
            // given
            TokenRefreshRequest request = TokenRefreshRequest.builder()
                    .refreshToken("expiredRefreshToken")
                    .build();

            given(jwtTokenProvider.validateTokenWithResult("expiredRefreshToken"))
                    .willReturn(JwtTokenProvider.TokenValidationResult.EXPIRED);

            // when & then
            assertThatThrownBy(() -> authService.refreshToken(request))
                    .isInstanceOf(CustomException.class)
                    .satisfies(ex -> {
                        CustomException ce = (CustomException) ex;
                        assertThat(ce.getErrorCode()).isEqualTo(ErrorCode.EXPIRED_TOKEN);
                    });
        }

        @Test
        @DisplayName("유효하지 않은 리프레시 토큰으로 갱신 실패")
        void refreshToken_InvalidToken_ThrowsException() {
            // given
            TokenRefreshRequest request = TokenRefreshRequest.builder()
                    .refreshToken("invalidRefreshToken")
                    .build();

            given(jwtTokenProvider.validateTokenWithResult("invalidRefreshToken"))
                    .willReturn(JwtTokenProvider.TokenValidationResult.INVALID);

            // when & then
            assertThatThrownBy(() -> authService.refreshToken(request))
                    .isInstanceOf(CustomException.class)
                    .satisfies(ex -> {
                        CustomException ce = (CustomException) ex;
                        assertThat(ce.getErrorCode()).isEqualTo(ErrorCode.INVALID_TOKEN);
                    });
        }
    }

    @Nested
    @DisplayName("내 정보 조회 테스트")
    class GetMyInfoTest {

        @Test
        @DisplayName("정상적인 내 정보 조회 성공")
        void getMyInfo_Success() {
            // given
            User user = User.builder()
                    .name("홍길동")
                    .email("test@test.com")
                    .userPassword("encodedPassword")
                    .role(guestRole)
                    .build();

            given(userRepository.findById(1L)).willReturn(Optional.of(user));
            given(userDomainRoleRepository.findByUserIdWithDomainAndRole(any())).willReturn(Collections.emptyList());

            // when
            MyInfoResponse response = authService.getMyInfo(1L);

            // then
            assertThat(response).isNotNull();
            assertThat(response.getEmail()).isEqualTo("test@test.com");
            assertThat(response.getName()).isEqualTo("홍길동");
            assertThat(response.getRole().getCode()).isEqualTo("GUEST");
        }

        @Test
        @DisplayName("존재하지 않는 사용자 ID로 조회 실패")
        void getMyInfo_UserNotFound_ThrowsException() {
            // given
            given(userRepository.findById(999L)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> authService.getMyInfo(999L))
                    .isInstanceOf(CustomException.class)
                    .satisfies(ex -> {
                        CustomException ce = (CustomException) ex;
                        assertThat(ce.getErrorCode()).isEqualTo(ErrorCode.USER_NOT_FOUND);
                    });
        }
    }

    @Nested
    @DisplayName("내 도메인 역할 조회 테스트")
    class GetMyDomainsTest {

        @Test
        @DisplayName("일반 사용자: 도메인 역할 목록 반환")
        void getMyDomains_WithDomainRoles_Success() {
            // given
            Role drafterRole = new Role("기안자", "DRAFTER");
            User user = User.builder()
                    .name("홍길동")
                    .email("test@test.com")
                    .userPassword("encodedPassword")
                    .role(drafterRole)
                    .build();

            Domain esgDomain = mock(Domain.class);
            lenient().when(esgDomain.getCode()).thenReturn("ESG");
            lenient().when(esgDomain.getName()).thenReturn("ESG 실사");

            Role mockDrafterRole = mock(Role.class);
            lenient().when(mockDrafterRole.getCode()).thenReturn("DRAFTER");
            lenient().when(mockDrafterRole.getName()).thenReturn("기안자");

            Domain safetyDomain = mock(Domain.class);
            lenient().when(safetyDomain.getCode()).thenReturn("SAFETY");
            lenient().when(safetyDomain.getName()).thenReturn("안전보건");

            UserDomainRole udr1 = mock(UserDomainRole.class);
            lenient().when(udr1.getDomain()).thenReturn(esgDomain);
            lenient().when(udr1.getRole()).thenReturn(mockDrafterRole);

            UserDomainRole udr2 = mock(UserDomainRole.class);
            lenient().when(udr2.getDomain()).thenReturn(safetyDomain);
            lenient().when(udr2.getRole()).thenReturn(mockDrafterRole);

            given(userRepository.findById(1L)).willReturn(Optional.of(user));
            given(userDomainRoleRepository.findByUserIdWithDomainAndRole(any()))
                    .willReturn(List.of(udr1, udr2));

            // when
            MyDomainResponse response = authService.getMyDomains(1L);

            // then
            assertThat(response.getGlobalRole()).isEqualTo("DRAFTER");
            assertThat(response.getDomainRoles()).hasSize(2);
            assertThat(response.getDomainRoles().get(0).getDomainCode()).isEqualTo("ESG");
            assertThat(response.getDomainRoles().get(1).getDomainCode()).isEqualTo("SAFETY");
            assertThat(response.getRoleRequestStatus()).isNull();
        }

        @Test
        @DisplayName("게스트 사용자: 빈 목록 + 권한요청 상태 PENDING")
        void getMyDomains_Guest_PendingRequest() {
            // given
            User user = User.builder()
                    .name("게스트")
                    .email("guest@test.com")
                    .userPassword("encodedPassword")
                    .role(guestRole)
                    .build();

            given(userRepository.findById(1L)).willReturn(Optional.of(user));
            given(userDomainRoleRepository.findByUserIdWithDomainAndRole(any()))
                    .willReturn(Collections.emptyList());
            given(roleRequestRepository.existsByUserAndStatus(user, RequestStatus.PENDING))
                    .willReturn(true);

            // when
            MyDomainResponse response = authService.getMyDomains(1L);

            // then
            assertThat(response.getGlobalRole()).isEqualTo("GUEST");
            assertThat(response.getDomainRoles()).isEmpty();
            assertThat(response.getRoleRequestStatus()).isEqualTo("PENDING");
        }

        @Test
        @DisplayName("게스트 사용자: 빈 목록 + 권한요청 없음 NONE")
        void getMyDomains_Guest_NoRequest() {
            // given
            User user = User.builder()
                    .name("게스트")
                    .email("guest@test.com")
                    .userPassword("encodedPassword")
                    .role(guestRole)
                    .build();

            given(userRepository.findById(1L)).willReturn(Optional.of(user));
            given(userDomainRoleRepository.findByUserIdWithDomainAndRole(any()))
                    .willReturn(Collections.emptyList());
            given(roleRequestRepository.existsByUserAndStatus(user, RequestStatus.PENDING))
                    .willReturn(false);

            // when
            MyDomainResponse response = authService.getMyDomains(1L);

            // then
            assertThat(response.getGlobalRole()).isEqualTo("GUEST");
            assertThat(response.getDomainRoles()).isEmpty();
            assertThat(response.getRoleRequestStatus()).isEqualTo("NONE");
        }
    }
}
