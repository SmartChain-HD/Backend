package com.smartchain.platform.integration;

import com.smartchain.platform.domain.user.entity.*;
import com.smartchain.platform.domain.user.repository.*;
import com.smartchain.platform.global.security.JwtTokenProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
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
 * API 스모크 테스트 - 엔드포인트 접근 가능 여부 확인
 * Issue #16: API 통합 테스트 작성
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@DisplayName("API 스모크 테스트")
class ApiSmokeTest {

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

    private String validToken;

    @BeforeEach
    void setUp() {
        // Basic setup for token generation
        Role drafterRole = roleRepository.save(new Role("기안자", "DRAFTER"));
        Industry industry = industryRepository.save(new Industry("제조업", "MANUFACTURING"));
        Company company = companyRepository.save(Company.builder()
                .industry(industry)
                .name("(주)테스트회사")
                .scale("중소기업")
                .businessNumber("123-45-67890")
                .ceoName("테스트")
                .address("서울시")
                .contactEmail("test@test.com")
                .contactPhone("02-1234-5678")
                .build());
        User user = userRepository.save(User.builder()
                .name("테스트")
                .email("test@test.com")
                .userPassword("password123")
                .company(company)
                .role(drafterRole)
                .build());
        validToken = jwtTokenProvider.createAccessToken(user.getUserId(), user.getEmail(), "DRAFTER");
    }

    @Test
    @DisplayName("Health 엔드포인트 접근 가능")
    void healthEndpoint_IsAccessible() throws Exception {
        mockMvc.perform(get("/health"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Diagnostic 목록 API 응답 확인")
    void diagnosticListEndpoint_ReturnsResponse() throws Exception {
        mockMvc.perform(get("/api/v1/diagnostics")
                        .header("Authorization", "Bearer " + validToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("존재하지 않는 기안 조회 시 적절한 에러 응답")
    void diagnosticNotFound_Returns404() throws Exception {
        mockMvc.perform(get("/api/v1/diagnostics/99999")
                        .header("Authorization", "Bearer " + validToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

}
