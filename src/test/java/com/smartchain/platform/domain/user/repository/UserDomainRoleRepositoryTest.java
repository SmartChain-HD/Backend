package com.smartchain.platform.domain.user.repository;

import com.smartchain.platform.domain.user.entity.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * UserDomainRoleRepository 통합 테스트
 * Issue #15: 도메인 권한 체계 단위 테스트 작성
 */
@DataJpaTest
@ActiveProfiles("test")
@DisplayName("UserDomainRoleRepository 테스트")
class UserDomainRoleRepositoryTest {

    @Autowired
    private UserDomainRoleRepository userDomainRoleRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private DomainRepository domainRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private CompanyRepository companyRepository;

    @Autowired
    private IndustryRepository industryRepository;

    private User testUser;
    private User anotherUser;
    private Domain esgDomain;
    private Domain safetyDomain;
    private Domain complianceDomain;
    private Role drafterRole;
    private Role approverRole;
    private Role reviewerRole;
    private Company testCompany;

    @BeforeEach
    void setUp() {
        // Domain 생성
        esgDomain = domainRepository.save(Domain.builder()
                .code("ESG")
                .name("ESG 실사")
                .description("ESG 공급망 실사")
                .isActive(true)
                .build());

        safetyDomain = domainRepository.save(Domain.builder()
                .code("SAFETY")
                .name("안전보건")
                .description("안전보건 관리")
                .isActive(true)
                .build());

        complianceDomain = domainRepository.save(Domain.builder()
                .code("COMPLIANCE")
                .name("컴플라이언스")
                .description("컴플라이언스 관리")
                .isActive(true)
                .build());

        // Role 생성
        drafterRole = roleRepository.save(new Role("기안자", "DRAFTER"));
        approverRole = roleRepository.save(new Role("결재자", "APPROVER"));
        reviewerRole = roleRepository.save(new Role("수신자", "REVIEWER"));

        // Industry 생성
        Industry industry = industryRepository.save(new Industry("제조업", "MANUFACTURING"));

        // Company 생성
        testCompany = companyRepository.save(Company.builder()
                .industry(industry)
                .name("(주)테스트회사")
                .scale("중소기업")
                .businessNumber("123-45-67890")
                .ceoName("테스트")
                .address("서울시")
                .contactEmail("test@test.com")
                .contactPhone("02-1234-5678")
                .build());

        // User 생성
        testUser = userRepository.save(User.builder()
                .name("테스트 사용자")
                .email("test@example.com")
                .userPassword("password123")
                .company(testCompany)
                .role(drafterRole)
                .build());

        anotherUser = userRepository.save(User.builder()
                .name("다른 사용자")
                .email("another@example.com")
                .userPassword("password456")
                .company(testCompany)
                .role(approverRole)
                .build());
    }

    @Nested
    @DisplayName("findByUser() 테스트")
    class FindByUserTest {

        @Test
        @DisplayName("사용자의 모든 도메인 역할 조회")
        void findByUser_ReturnsAllDomainRoles() {
            // given
            userDomainRoleRepository.save(UserDomainRole.builder()
                    .user(testUser)
                    .domain(esgDomain)
                    .role(drafterRole)
                    .build());

            userDomainRoleRepository.save(UserDomainRole.builder()
                    .user(testUser)
                    .domain(safetyDomain)
                    .role(approverRole)
                    .build());

            // when
            List<UserDomainRole> result = userDomainRoleRepository.findByUser(testUser);

            // then
            assertThat(result).hasSize(2);
            assertThat(result).extracting(udr -> udr.getDomain().getCode())
                    .containsExactlyInAnyOrder("ESG", "SAFETY");
        }

        @Test
        @DisplayName("도메인 역할이 없는 사용자는 빈 목록 반환")
        void findByUser_NoDomainRoles_ReturnsEmptyList() {
            // when
            List<UserDomainRole> result = userDomainRoleRepository.findByUser(testUser);

            // then
            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("findByUserUserId() 테스트")
    class FindByUserUserIdTest {

        @Test
        @DisplayName("사용자 ID로 도메인 역할 조회")
        void findByUserUserId_ReturnsRoles() {
            // given
            userDomainRoleRepository.save(UserDomainRole.builder()
                    .user(testUser)
                    .domain(esgDomain)
                    .role(drafterRole)
                    .build());

            // when
            List<UserDomainRole> result = userDomainRoleRepository.findByUserUserId(testUser.getUserId());

            // then
            assertThat(result).hasSize(1);
            assertThat(result.get(0).getDomain().getCode()).isEqualTo("ESG");
        }
    }

    @Nested
    @DisplayName("findByUserAndDomain() 테스트")
    class FindByUserAndDomainTest {

        @Test
        @DisplayName("특정 사용자의 특정 도메인 역할 조회")
        void findByUserAndDomain_ReturnsRole() {
            // given
            UserDomainRole savedRole = userDomainRoleRepository.save(UserDomainRole.builder()
                    .user(testUser)
                    .domain(esgDomain)
                    .role(drafterRole)
                    .build());

            // when
            Optional<UserDomainRole> result = userDomainRoleRepository.findByUserAndDomain(testUser, esgDomain);

            // then
            assertThat(result).isPresent();
            assertThat(result.get().getRole().getCode()).isEqualTo("DRAFTER");
        }

        @Test
        @DisplayName("존재하지 않는 도메인 역할은 빈 Optional 반환")
        void findByUserAndDomain_NotExists_ReturnsEmpty() {
            // when
            Optional<UserDomainRole> result = userDomainRoleRepository.findByUserAndDomain(testUser, esgDomain);

            // then
            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("existsByUserAndDomain() 테스트")
    class ExistsByUserAndDomainTest {

        @Test
        @DisplayName("도메인 역할 존재 여부 확인 - 존재함")
        void existsByUserAndDomain_Exists_ReturnsTrue() {
            // given
            userDomainRoleRepository.save(UserDomainRole.builder()
                    .user(testUser)
                    .domain(esgDomain)
                    .role(drafterRole)
                    .build());

            // when
            boolean result = userDomainRoleRepository.existsByUserAndDomain(testUser, esgDomain);

            // then
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("도메인 역할 존재 여부 확인 - 존재하지 않음")
        void existsByUserAndDomain_NotExists_ReturnsFalse() {
            // when
            boolean result = userDomainRoleRepository.existsByUserAndDomain(testUser, esgDomain);

            // then
            assertThat(result).isFalse();
        }
    }

    @Nested
    @DisplayName("findByUserIdWithDomainAndRole() 테스트")
    class FindByUserIdWithDomainAndRoleTest {

        @Test
        @DisplayName("사용자 ID로 도메인과 역할을 페치 조인하여 조회")
        void findByUserIdWithDomainAndRole_FetchesAll() {
            // given
            userDomainRoleRepository.save(UserDomainRole.builder()
                    .user(testUser)
                    .domain(esgDomain)
                    .role(drafterRole)
                    .build());

            userDomainRoleRepository.save(UserDomainRole.builder()
                    .user(testUser)
                    .domain(safetyDomain)
                    .role(approverRole)
                    .build());

            // when
            List<UserDomainRole> result = userDomainRoleRepository.findByUserIdWithDomainAndRole(testUser.getUserId());

            // then
            assertThat(result).hasSize(2);
            // N+1 문제 없이 조회되었는지 검증 (페치 조인)
            assertThat(result.get(0).getDomain()).isNotNull();
            assertThat(result.get(0).getRole()).isNotNull();
        }
    }

    @Nested
    @DisplayName("findByUserIdAndDomainCode() 테스트")
    class FindByUserIdAndDomainCodeTest {

        @Test
        @DisplayName("사용자 ID와 도메인 코드로 역할 조회")
        void findByUserIdAndDomainCode_ReturnsRole() {
            // given
            userDomainRoleRepository.save(UserDomainRole.builder()
                    .user(testUser)
                    .domain(esgDomain)
                    .role(drafterRole)
                    .build());

            // when
            Optional<UserDomainRole> result = userDomainRoleRepository
                    .findByUserIdAndDomainCode(testUser.getUserId(), "ESG");

            // then
            assertThat(result).isPresent();
            assertThat(result.get().getRole().getCode()).isEqualTo("DRAFTER");
        }

        @Test
        @DisplayName("존재하지 않는 도메인 코드로 조회 시 빈 Optional 반환")
        void findByUserIdAndDomainCode_InvalidCode_ReturnsEmpty() {
            // given
            userDomainRoleRepository.save(UserDomainRole.builder()
                    .user(testUser)
                    .domain(esgDomain)
                    .role(drafterRole)
                    .build());

            // when
            Optional<UserDomainRole> result = userDomainRoleRepository
                    .findByUserIdAndDomainCode(testUser.getUserId(), "INVALID");

            // then
            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("deleteByUserAndDomain() 테스트")
    class DeleteByUserAndDomainTest {

        @Test
        @DisplayName("특정 사용자의 특정 도메인 역할 삭제")
        void deleteByUserAndDomain_DeletesRole() {
            // given
            userDomainRoleRepository.save(UserDomainRole.builder()
                    .user(testUser)
                    .domain(esgDomain)
                    .role(drafterRole)
                    .build());

            userDomainRoleRepository.save(UserDomainRole.builder()
                    .user(testUser)
                    .domain(safetyDomain)
                    .role(approverRole)
                    .build());

            // when
            userDomainRoleRepository.deleteByUserAndDomain(testUser, esgDomain);

            // then
            List<UserDomainRole> remaining = userDomainRoleRepository.findByUser(testUser);
            assertThat(remaining).hasSize(1);
            assertThat(remaining.get(0).getDomain().getCode()).isEqualTo("SAFETY");
        }
    }

    @Nested
    @DisplayName("복합 시나리오 테스트")
    class ComplexScenarioTest {

        @Test
        @DisplayName("여러 사용자의 도메인 역할이 독립적으로 관리됨")
        void multipleUsers_IndependentDomainRoles() {
            // given
            userDomainRoleRepository.save(UserDomainRole.builder()
                    .user(testUser)
                    .domain(esgDomain)
                    .role(drafterRole)
                    .build());

            userDomainRoleRepository.save(UserDomainRole.builder()
                    .user(anotherUser)
                    .domain(esgDomain)
                    .role(reviewerRole)
                    .build());

            // when
            List<UserDomainRole> testUserRoles = userDomainRoleRepository.findByUser(testUser);
            List<UserDomainRole> anotherUserRoles = userDomainRoleRepository.findByUser(anotherUser);

            // then
            assertThat(testUserRoles).hasSize(1);
            assertThat(testUserRoles.get(0).getRole().getCode()).isEqualTo("DRAFTER");

            assertThat(anotherUserRoles).hasSize(1);
            assertThat(anotherUserRoles.get(0).getRole().getCode()).isEqualTo("REVIEWER");
        }

        @Test
        @DisplayName("동일 사용자가 여러 도메인에서 다른 역할 보유")
        void singleUser_MultipleDomains_DifferentRoles() {
            // given
            userDomainRoleRepository.save(UserDomainRole.builder()
                    .user(testUser)
                    .domain(esgDomain)
                    .role(drafterRole)
                    .build());

            userDomainRoleRepository.save(UserDomainRole.builder()
                    .user(testUser)
                    .domain(safetyDomain)
                    .role(approverRole)
                    .build());

            userDomainRoleRepository.save(UserDomainRole.builder()
                    .user(testUser)
                    .domain(complianceDomain)
                    .role(reviewerRole)
                    .build());

            // when
            List<UserDomainRole> roles = userDomainRoleRepository.findByUserIdWithDomainAndRole(testUser.getUserId());

            // then
            assertThat(roles).hasSize(3);
            assertThat(roles).extracting(udr -> udr.getDomain().getCode())
                    .containsExactlyInAnyOrder("ESG", "SAFETY", "COMPLIANCE");
            assertThat(roles).extracting(udr -> udr.getRole().getCode())
                    .containsExactlyInAnyOrder("DRAFTER", "APPROVER", "REVIEWER");
        }

        @Test
        @DisplayName("역할 변경 테스트")
        void changeRole_Success() {
            // given
            UserDomainRole savedRole = userDomainRoleRepository.save(UserDomainRole.builder()
                    .user(testUser)
                    .domain(esgDomain)
                    .role(drafterRole)
                    .build());

            // when
            savedRole.changeRole(approverRole);
            userDomainRoleRepository.save(savedRole);

            // then
            Optional<UserDomainRole> updated = userDomainRoleRepository
                    .findByUserAndDomain(testUser, esgDomain);
            assertThat(updated).isPresent();
            assertThat(updated.get().getRole().getCode()).isEqualTo("APPROVER");
        }
    }
}
