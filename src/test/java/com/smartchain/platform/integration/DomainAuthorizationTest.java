package com.smartchain.platform.integration;

import com.smartchain.platform.domain.user.entity.*;
import com.smartchain.platform.domain.user.repository.*;
import com.smartchain.platform.global.security.JwtTokenProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * 도메인 권한 검증 통합 테스트
 * Issue #52: API 통합 테스트 확장
 *
 * 역할별 API 접근 제한 검증
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@DisplayName("도메인 권한 검증 통합 테스트")
class DomainAuthorizationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private CompanyRepository companyRepository;

    @Autowired
    private IndustryRepository industryRepository;

    @Autowired
    private DomainRepository domainRepository;

    @Autowired
    private UserDomainRoleRepository userDomainRoleRepository;

    private User drafterUser;
    private User guestUser;
    private String drafterToken;
    private String guestToken;
    private Company company;

    @BeforeEach
    void setUp() {
        // 역할 설정
        Role drafterRole = roleRepository.save(new Role("기안자", "DRAFTER"));
        Role guestRole = roleRepository.save(new Role("게스트", "GUEST"));

        // 도메인 설정
        Domain esgDomain = domainRepository.save(Domain.builder()
                .code("ESG")
                .name("ESG 실사")
                .description("ESG 실사 도메인")
                .isActive(true)
                .build());

        // 회사 설정
        Industry industry = industryRepository.save(new Industry("제조업", "MANUFACTURING"));
        company = companyRepository.save(Company.builder()
                .industry(industry)
                .name("(주)권한테스트회사")
                .scale("중소기업")
                .businessNumber("567-89-01234")
                .ceoName("권한대표")
                .address("서울시 마포구")
                .contactEmail("auth@test.com")
                .contactPhone("02-5678-9012")
                .build());

        // 기안자 설정
        drafterUser = User.builder()
                .name("기안자")
                .email("drafter-auth@test.com")
                .userPassword("password123")
                .company(company)
                .role(drafterRole)
                .build();
        UserDomainRole drafterDomainRole = UserDomainRole.builder()
                .user(drafterUser)
                .domain(esgDomain)
                .role(drafterRole)
                .build();
        drafterUser.addDomainRole(drafterDomainRole);
        drafterUser = userRepository.save(drafterUser);

        // 게스트 설정 (도메인 역할 없음)
        guestUser = userRepository.save(User.builder()
                .name("게스트")
                .email("guest-auth@test.com")
                .userPassword("password123")
                .company(company)
                .role(guestRole)
                .build());

        // JWT 토큰 생성
        drafterToken = jwtTokenProvider.createAccessToken(drafterUser.getUserId(), drafterUser.getEmail(), "DRAFTER");
        guestToken = jwtTokenProvider.createAccessToken(guestUser.getUserId(), guestUser.getEmail(), "GUEST");
    }

    @Nested
    @DisplayName("GUEST 역할 접근 제한")
    class GuestRoleRestriction {

        @Test
        @DisplayName("GUEST가 기안 목록 조회 시 403 응답")
        void getDiagnosticList_ByGuest_Forbidden() throws Exception {
            mockMvc.perform(get("/api/v1/diagnostics")
                            .header("Authorization", "Bearer " + guestToken)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("GUEST가 결재 목록 조회 시 403 응답")
        void getApprovalList_ByGuest_Forbidden() throws Exception {
            mockMvc.perform(get("/api/v1/approvals")
                            .header("Authorization", "Bearer " + guestToken)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("GUEST가 심사 대시보드 조회 시 403 응답")
        void getReviewDashboard_ByGuest_Forbidden() throws Exception {
            mockMvc.perform(get("/api/v1/reviews/dashboard")
                            .header("Authorization", "Bearer " + guestToken)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isForbidden());
        }
    }

    @Nested
    @DisplayName("역할 불일치 시나리오")
    class RoleMismatchRestriction {

        @Test
        @DisplayName("DRAFTER가 결재 목록 조회 시 403 응답")
        void getApprovalList_ByDrafter_Forbidden() throws Exception {
            mockMvc.perform(get("/api/v1/approvals")
                            .header("Authorization", "Bearer " + drafterToken)
                            .param("domainCode", "ESG")  // domainCode 필수 (#162)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("DRAFTER가 심사 대시보드 조회 시 403 응답")
        void getReviewDashboard_ByDrafter_Forbidden() throws Exception {
            mockMvc.perform(get("/api/v1/reviews/dashboard")
                            .header("Authorization", "Bearer " + drafterToken)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isForbidden());
        }
    }
}
