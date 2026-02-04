package com.smartchain.platform.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.smartchain.platform.domain.approval.entity.Approval;
import com.smartchain.platform.domain.approval.repository.ApprovalRepository;
import com.smartchain.platform.domain.diagnostic.entity.Campaign;
import com.smartchain.platform.domain.diagnostic.entity.Diagnostic;
import com.smartchain.platform.domain.diagnostic.repository.CampaignRepository;
import com.smartchain.platform.domain.diagnostic.repository.DiagnosticRepository;
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

import java.time.LocalDate;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * ApprovalController 상세 통합 테스트
 * Issue #52: API 통합 테스트 확장
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@DisplayName("ApprovalController 통합 테스트")
class ApprovalIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

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

    @Autowired
    private CampaignRepository campaignRepository;

    @Autowired
    private DiagnosticRepository diagnosticRepository;

    @Autowired
    private ApprovalRepository approvalRepository;

    private User drafterUser;
    private User approverUser;
    private String approverToken;
    private Domain esgDomain;
    private Campaign campaign;
    private Company company;
    private Role approverRole;

    @BeforeEach
    void setUp() {
        // 역할 설정
        Role drafterRole = roleRepository.save(new Role("기안자", "DRAFTER"));
        approverRole = roleRepository.save(new Role("결재자", "APPROVER"));

        // 도메인 설정
        esgDomain = domainRepository.save(Domain.builder()
                .code("ESG")
                .name("ESG 실사")
                .description("ESG 실사 도메인")
                .isActive(true)
                .build());

        // 회사 설정
        Industry industry = industryRepository.save(new Industry("제조업", "MANUFACTURING"));
        company = companyRepository.save(Company.builder()
                .industry(industry)
                .name("(주)결재테스트회사")
                .scale("중소기업")
                .businessNumber("234-56-78901")
                .ceoName("결재대표")
                .address("서울시 서초구")
                .contactEmail("approval@test.com")
                .contactPhone("02-2345-6789")
                .build());

        // 기안자 사용자 설정
        drafterUser = User.builder()
                .name("기안자")
                .email("drafter-approval@test.com")
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

        // 결재자 사용자 설정
        approverUser = User.builder()
                .name("결재자")
                .email("approver-approval@test.com")
                .userPassword("password123")
                .company(company)
                .role(approverRole)
                .build();
        UserDomainRole approverDomainRole = UserDomainRole.builder()
                .user(approverUser)
                .domain(esgDomain)
                .role(approverRole)
                .build();
        approverUser.addDomainRole(approverDomainRole);
        approverUser = userRepository.save(approverUser);

        // 캠페인 설정
        campaign = campaignRepository.save(Campaign.builder()
                .campaignCode("CAMP-APPR-001")
                .ownerCompanyId(1L)
                .domain(esgDomain)
                .title("결재 테스트 캠페인")
                .content("결재 테스트용 캠페인")
                .periodStartDate(LocalDate.of(2026, 1, 1))
                .periodEndDate(LocalDate.of(2026, 3, 31))
                .deadline(LocalDate.of(2026, 2, 28))
                .build());

        // JWT 토큰 생성
        approverToken = jwtTokenProvider.createAccessToken(approverUser.getUserId(), approverUser.getEmail(), "APPROVER");
    }

    private Diagnostic createSubmittedDiagnostic(String code) {
        Diagnostic diagnostic = diagnosticRepository.save(Diagnostic.builder()
                .diagnosticCode(code)
                .title("결재 대기 기안")
                .campaign(campaign)
                .company(company)
                .domain(esgDomain)
                .drafterId(drafterUser.getUserId())
                .periodStartDate(LocalDate.of(2026, 1, 1))
                .periodEndDate(LocalDate.of(2026, 3, 31))
                .deadline(LocalDate.of(2026, 2, 28))
                .build());
        diagnostic.submit();
        return diagnosticRepository.save(diagnostic);
    }

    private Approval createApproval(Diagnostic diagnostic) {
        return approvalRepository.save(Approval.builder()
                .diagnostic(diagnostic)
                .requester(drafterUser)
                .requestComment("결재 요청합니다")
                .deadline(LocalDate.of(2026, 2, 28))
                .build());
    }

    @Nested
    @DisplayName("결재 목록 조회")
    class GetApprovalList {

        @Test
        @DisplayName("결재 목록 조회 성공")
        void getApprovalList_Success() throws Exception {
            mockMvc.perform(get("/api/v1/approvals")
                            .header("Authorization", "Bearer " + approverToken)
                            .param("domainCode", "ESG")  // domainCode 필수 (#162)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data").exists());
        }

        @Test
        @DisplayName("도메인 코드로 필터링")
        void getApprovalList_FilterByDomainCode() throws Exception {
            mockMvc.perform(get("/api/v1/approvals")
                            .header("Authorization", "Bearer " + approverToken)
                            .param("domainCode", "ESG")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true));
        }

        @Test
        @DisplayName("상태로 필터링")
        void getApprovalList_FilterByStatus() throws Exception {
            mockMvc.perform(get("/api/v1/approvals")
                            .header("Authorization", "Bearer " + approverToken)
                            .param("domainCode", "ESG")  // domainCode 필수 (#162)
                            .param("status", "WAITING")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true));
        }
    }

    @Nested
    @DisplayName("결재 상세 조회")
    class GetApprovalDetail {

        @Test
        @DisplayName("존재하지 않는 결재 조회 시 404 응답")
        void getApprovalDetail_NotFound() throws Exception {
            mockMvc.perform(get("/api/v1/approvals/{approvalId}", 99999L)
                            .header("Authorization", "Bearer " + approverToken)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("결재 처리")
    class ProcessApproval {

        @Test
        @DisplayName("결재 승인 성공")
        void processApproval_Approve_Success() throws Exception {
            // Given
            Diagnostic diagnostic = createSubmittedDiagnostic("DIAG-APPR-APPROVE");
            Approval approval = createApproval(diagnostic);

            String decisionRequest = """
                {
                    "decision": "APPROVED",
                    "comment": "승인합니다"
                }
                """;

            mockMvc.perform(patch("/api/v1/approvals/{approvalId}", approval.getApprovalId())
                            .header("Authorization", "Bearer " + approverToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(decisionRequest))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true));
        }

        @Test
        @DisplayName("결재 반려 성공")
        void processApproval_Reject_Success() throws Exception {
            // Given
            Diagnostic diagnostic = createSubmittedDiagnostic("DIAG-APPR-REJECT");
            Approval approval = createApproval(diagnostic);

            String decisionRequest = """
                {
                    "decision": "REJECTED",
                    "comment": "보완 후 재제출 바랍니다"
                }
                """;

            mockMvc.perform(patch("/api/v1/approvals/{approvalId}", approval.getApprovalId())
                            .header("Authorization", "Bearer " + approverToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(decisionRequest))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true));
        }
    }
}
