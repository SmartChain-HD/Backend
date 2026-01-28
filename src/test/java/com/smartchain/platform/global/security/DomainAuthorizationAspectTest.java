package com.smartchain.platform.global.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.smartchain.platform.domain.user.entity.User;
import com.smartchain.platform.domain.user.repository.UserRepository;
import com.smartchain.platform.global.error.CustomException;
import com.smartchain.platform.global.error.ErrorCode;
import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.JoinPoint;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DomainAuthorizationAspectTest {

    @InjectMocks
    private DomainAuthorizationAspect aspect;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @Mock
    private UserRepository userRepository;

    @Mock
    private JoinPoint joinPoint;

    @Mock
    private DomainAuthorization domainAuthorization;

    @Mock
    private HttpServletRequest request;

    @Mock
    private User user;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        // Replace ObjectMapper mock with real instance via reflection
        try {
            var field = DomainAuthorizationAspect.class.getDeclaredField("objectMapper");
            field.setAccessible(true);
            field.set(aspect, objectMapper);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));
    }

    @Nested
    @DisplayName("도메인 권한 검증 테스트")
    class CheckDomainAuthorizationTest {

        @Test
        @DisplayName("요청 파라미터에서 domainCode 추출 후 권한 검증 성공")
        void checkAuth_WithRequestParam_Success() {
            // given
            when(domainAuthorization.roles()).thenReturn(new String[]{"DRAFTER", "APPROVER"});
            when(domainAuthorization.domainCodeParam()).thenReturn("domainCode");
            when(request.getHeader("Authorization")).thenReturn("Bearer test-token");
            when(jwtTokenProvider.getUserIdFromToken("test-token")).thenReturn(1L);
            when(request.getParameter("domainCode")).thenReturn("ENV");
            when(userRepository.findById(1L)).thenReturn(Optional.of(user));
            when(user.hasAnyRoleInDomain("ENV", new String[]{"DRAFTER", "APPROVER"})).thenReturn(true);
            when(joinPoint.getArgs()).thenReturn(new Object[]{request});

            // when & then
            assertThatCode(() -> aspect.checkDomainAuthorization(joinPoint, domainAuthorization))
                    .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("도메인 권한이 없으면 PERMISSION_DENIED_ACTION 에러")
        void checkAuth_WithoutPermission_ThrowsException() {
            // given
            when(domainAuthorization.roles()).thenReturn(new String[]{"REVIEWER"});
            when(domainAuthorization.domainCodeParam()).thenReturn("domainCode");
            when(request.getHeader("Authorization")).thenReturn("Bearer test-token");
            when(jwtTokenProvider.getUserIdFromToken("test-token")).thenReturn(1L);
            when(request.getParameter("domainCode")).thenReturn("ENV");
            when(userRepository.findById(1L)).thenReturn(Optional.of(user));
            when(user.hasAnyRoleInDomain("ENV", new String[]{"REVIEWER"})).thenReturn(false);
            when(joinPoint.getArgs()).thenReturn(new Object[]{request});

            // when & then
            assertThatThrownBy(() -> aspect.checkDomainAuthorization(joinPoint, domainAuthorization))
                    .isInstanceOf(CustomException.class)
                    .satisfies(ex -> {
                        CustomException ce = (CustomException) ex;
                        assert ce.getErrorCode() == ErrorCode.PERMISSION_DENIED_ACTION;
                    });
        }

        @Test
        @DisplayName("domainCode가 요청에 없으면 INVALID_INPUT 에러")
        void checkAuth_MissingDomainCode_ThrowsException() {
            // given
            when(domainAuthorization.roles()).thenReturn(new String[]{"DRAFTER"});
            when(domainAuthorization.domainCodeParam()).thenReturn("domainCode");
            when(request.getHeader("Authorization")).thenReturn("Bearer test-token");
            when(jwtTokenProvider.getUserIdFromToken("test-token")).thenReturn(1L);
            when(request.getParameter("domainCode")).thenReturn(null);
            when(joinPoint.getArgs()).thenReturn(new Object[]{request});

            // when & then
            assertThatThrownBy(() -> aspect.checkDomainAuthorization(joinPoint, domainAuthorization))
                    .isInstanceOf(CustomException.class)
                    .satisfies(ex -> {
                        CustomException ce = (CustomException) ex;
                        assert ce.getErrorCode() == ErrorCode.INVALID_INPUT;
                    });
        }

        @Test
        @DisplayName("Authorization 헤더가 없으면 INVALID_TOKEN 에러")
        void checkAuth_MissingToken_ThrowsException() {
            // given
            when(domainAuthorization.roles()).thenReturn(new String[]{"DRAFTER"});
            when(domainAuthorization.domainCodeParam()).thenReturn("domainCode");
            when(request.getHeader("Authorization")).thenReturn(null);

            // when & then
            assertThatThrownBy(() -> aspect.checkDomainAuthorization(joinPoint, domainAuthorization))
                    .isInstanceOf(CustomException.class)
                    .satisfies(ex -> {
                        CustomException ce = (CustomException) ex;
                        assert ce.getErrorCode() == ErrorCode.INVALID_TOKEN;
                    });
        }

        @Test
        @DisplayName("RequestBody에서 domainCode 추출 후 권한 검증 성공")
        void checkAuth_WithRequestBody_Success() {
            // given
            when(domainAuthorization.roles()).thenReturn(new String[]{"DRAFTER"});
            when(domainAuthorization.domainCodeParam()).thenReturn("domainCode");
            when(request.getHeader("Authorization")).thenReturn("Bearer test-token");
            when(jwtTokenProvider.getUserIdFromToken("test-token")).thenReturn(1L);
            when(request.getParameter("domainCode")).thenReturn(null); // 파라미터에 없음

            // RequestBody 객체 시뮬레이션
            TestRequestBody body = new TestRequestBody("ENV", "test");
            when(joinPoint.getArgs()).thenReturn(new Object[]{request, body});
            when(userRepository.findById(1L)).thenReturn(Optional.of(user));
            when(user.hasAnyRoleInDomain("ENV", new String[]{"DRAFTER"})).thenReturn(true);

            // when & then
            assertThatCode(() -> aspect.checkDomainAuthorization(joinPoint, domainAuthorization))
                    .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("사용자를 찾을 수 없으면 USER_NOT_FOUND 에러")
        void checkAuth_UserNotFound_ThrowsException() {
            // given
            when(domainAuthorization.roles()).thenReturn(new String[]{"DRAFTER"});
            when(domainAuthorization.domainCodeParam()).thenReturn("domainCode");
            when(request.getHeader("Authorization")).thenReturn("Bearer test-token");
            when(jwtTokenProvider.getUserIdFromToken("test-token")).thenReturn(999L);
            when(request.getParameter("domainCode")).thenReturn("ENV");
            when(userRepository.findById(999L)).thenReturn(Optional.empty());
            when(joinPoint.getArgs()).thenReturn(new Object[]{request});

            // when & then
            assertThatThrownBy(() -> aspect.checkDomainAuthorization(joinPoint, domainAuthorization))
                    .isInstanceOf(CustomException.class)
                    .satisfies(ex -> {
                        CustomException ce = (CustomException) ex;
                        assert ce.getErrorCode() == ErrorCode.USER_NOT_FOUND;
                    });
        }
    }

    // RequestBody 시뮬레이션용 inner class
    static class TestRequestBody {
        private String domainCode;
        private String title;

        public TestRequestBody() {}

        public TestRequestBody(String domainCode, String title) {
            this.domainCode = domainCode;
            this.title = title;
        }

        public String getDomainCode() { return domainCode; }
        public void setDomainCode(String domainCode) { this.domainCode = domainCode; }
        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }
    }
}
