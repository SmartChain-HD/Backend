package com.smartchain.platform.domain.user.entity;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

/**
 * User 엔티티의 도메인 기반 권한 헬퍼 메서드 단위 테스트
 * Issue #15: 도메인 권한 체계 단위 테스트 작성
 */
@DisplayName("User 도메인 권한 메서드 테스트")
class UserDomainPermissionTest {

    private User user;
    private Domain esgDomain;
    private Domain safetyDomain;
    private Domain complianceDomain;
    private Role drafterRole;
    private Role approverRole;
    private Role reviewerRole;

    @BeforeEach
    void setUp() {
        // Mock Domain 생성
        esgDomain = mock(Domain.class);
        when(esgDomain.getDomainId()).thenReturn(1L);
        when(esgDomain.getCode()).thenReturn("ESG");
        when(esgDomain.getName()).thenReturn("ESG 실사");

        safetyDomain = mock(Domain.class);
        when(safetyDomain.getDomainId()).thenReturn(2L);
        when(safetyDomain.getCode()).thenReturn("SAFETY");
        when(safetyDomain.getName()).thenReturn("안전보건");

        complianceDomain = mock(Domain.class);
        when(complianceDomain.getDomainId()).thenReturn(3L);
        when(complianceDomain.getCode()).thenReturn("COMPLIANCE");
        when(complianceDomain.getName()).thenReturn("컴플라이언스");

        // Mock Role 생성
        drafterRole = mock(Role.class);
        when(drafterRole.getRoleId()).thenReturn(1L);
        when(drafterRole.getCode()).thenReturn("DRAFTER");
        when(drafterRole.getName()).thenReturn("기안자");

        approverRole = mock(Role.class);
        when(approverRole.getRoleId()).thenReturn(2L);
        when(approverRole.getCode()).thenReturn("APPROVER");
        when(approverRole.getName()).thenReturn("결재자");

        reviewerRole = mock(Role.class);
        when(reviewerRole.getRoleId()).thenReturn(3L);
        when(reviewerRole.getCode()).thenReturn("REVIEWER");
        when(reviewerRole.getName()).thenReturn("수신자");

        // User 생성 (실제 객체 사용 - 도메인 권한 메서드 테스트를 위해)
        user = User.builder()
                .name("테스트 사용자")
                .email("test@example.com")
                .userPassword("password")
                .build();
    }

    @Nested
    @DisplayName("getRoleForDomain() 테스트")
    class GetRoleForDomainTest {

        @Test
        @DisplayName("도메인에 역할이 있으면 해당 역할 반환")
        void getRoleForDomain_WithRole_ReturnsRole() {
            // given
            UserDomainRole udr = mock(UserDomainRole.class);
            when(udr.getDomain()).thenReturn(esgDomain);
            when(udr.getRole()).thenReturn(drafterRole);
            user.addDomainRole(udr);

            // when
            Optional<Role> result = user.getRoleForDomain("ESG");

            // then
            assertThat(result).isPresent();
            assertThat(result.get().getCode()).isEqualTo("DRAFTER");
        }

        @Test
        @DisplayName("도메인에 역할이 없으면 빈 Optional 반환")
        void getRoleForDomain_WithoutRole_ReturnsEmpty() {
            // given
            UserDomainRole udr = mock(UserDomainRole.class);
            when(udr.getDomain()).thenReturn(esgDomain);
            when(udr.getRole()).thenReturn(drafterRole);
            user.addDomainRole(udr);

            // when
            Optional<Role> result = user.getRoleForDomain("SAFETY");

            // then
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("도메인 역할이 없는 사용자는 빈 Optional 반환")
        void getRoleForDomain_NoDomainRoles_ReturnsEmpty() {
            // when
            Optional<Role> result = user.getRoleForDomain("ESG");

            // then
            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("hasRoleInDomain() 테스트")
    class HasRoleInDomainTest {

        @Test
        @DisplayName("해당 도메인에서 특정 역할을 가지고 있으면 true")
        void hasRoleInDomain_WithRole_ReturnsTrue() {
            // given
            UserDomainRole udr = mock(UserDomainRole.class);
            when(udr.getDomain()).thenReturn(esgDomain);
            when(udr.getRole()).thenReturn(drafterRole);
            user.addDomainRole(udr);

            // when
            boolean result = user.hasRoleInDomain("ESG", "DRAFTER");

            // then
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("해당 도메인에서 다른 역할이면 false")
        void hasRoleInDomain_WithDifferentRole_ReturnsFalse() {
            // given
            UserDomainRole udr = mock(UserDomainRole.class);
            when(udr.getDomain()).thenReturn(esgDomain);
            when(udr.getRole()).thenReturn(drafterRole);
            user.addDomainRole(udr);

            // when
            boolean result = user.hasRoleInDomain("ESG", "APPROVER");

            // then
            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("다른 도메인이면 false")
        void hasRoleInDomain_WithDifferentDomain_ReturnsFalse() {
            // given
            UserDomainRole udr = mock(UserDomainRole.class);
            when(udr.getDomain()).thenReturn(esgDomain);
            when(udr.getRole()).thenReturn(drafterRole);
            user.addDomainRole(udr);

            // when
            boolean result = user.hasRoleInDomain("SAFETY", "DRAFTER");

            // then
            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("도메인 역할이 없으면 false")
        void hasRoleInDomain_NoDomainRoles_ReturnsFalse() {
            // when
            boolean result = user.hasRoleInDomain("ESG", "DRAFTER");

            // then
            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("여러 도메인 역할 중 일치하는 것이 있으면 true")
        void hasRoleInDomain_MultipleRoles_MatchingOne_ReturnsTrue() {
            // given
            UserDomainRole esgDrafter = mock(UserDomainRole.class);
            when(esgDrafter.getDomain()).thenReturn(esgDomain);
            when(esgDrafter.getRole()).thenReturn(drafterRole);

            UserDomainRole safetyApprover = mock(UserDomainRole.class);
            when(safetyApprover.getDomain()).thenReturn(safetyDomain);
            when(safetyApprover.getRole()).thenReturn(approverRole);

            user.addDomainRole(esgDrafter);
            user.addDomainRole(safetyApprover);

            // when & then
            assertThat(user.hasRoleInDomain("ESG", "DRAFTER")).isTrue();
            assertThat(user.hasRoleInDomain("SAFETY", "APPROVER")).isTrue();
            assertThat(user.hasRoleInDomain("ESG", "APPROVER")).isFalse();
            assertThat(user.hasRoleInDomain("SAFETY", "DRAFTER")).isFalse();
        }
    }

    @Nested
    @DisplayName("hasAnyRoleInDomain() 테스트")
    class HasAnyRoleInDomainTest {

        @Test
        @DisplayName("주어진 역할들 중 하나를 가지고 있으면 true")
        void hasAnyRoleInDomain_WithMatchingRole_ReturnsTrue() {
            // given
            UserDomainRole udr = mock(UserDomainRole.class);
            when(udr.getDomain()).thenReturn(esgDomain);
            when(udr.getRole()).thenReturn(drafterRole);
            user.addDomainRole(udr);

            // when
            boolean result = user.hasAnyRoleInDomain("ESG", "DRAFTER", "APPROVER", "REVIEWER");

            // then
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("주어진 역할들 중 하나도 없으면 false")
        void hasAnyRoleInDomain_WithoutMatchingRole_ReturnsFalse() {
            // given
            UserDomainRole udr = mock(UserDomainRole.class);
            when(udr.getDomain()).thenReturn(esgDomain);
            when(udr.getRole()).thenReturn(drafterRole);
            user.addDomainRole(udr);

            // when
            boolean result = user.hasAnyRoleInDomain("ESG", "APPROVER", "REVIEWER");

            // then
            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("다른 도메인이면 false")
        void hasAnyRoleInDomain_DifferentDomain_ReturnsFalse() {
            // given
            UserDomainRole udr = mock(UserDomainRole.class);
            when(udr.getDomain()).thenReturn(esgDomain);
            when(udr.getRole()).thenReturn(drafterRole);
            user.addDomainRole(udr);

            // when
            boolean result = user.hasAnyRoleInDomain("SAFETY", "DRAFTER", "APPROVER");

            // then
            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("도메인 역할이 없으면 false")
        void hasAnyRoleInDomain_NoDomainRoles_ReturnsFalse() {
            // when
            boolean result = user.hasAnyRoleInDomain("ESG", "DRAFTER", "APPROVER", "REVIEWER");

            // then
            assertThat(result).isFalse();
        }
    }

    @Nested
    @DisplayName("getDomainsWithRole() 테스트")
    class GetDomainsWithRoleTest {

        @Test
        @DisplayName("특정 역할을 가진 도메인 목록 반환")
        void getDomainsWithRole_WithRoles_ReturnsDomains() {
            // given
            UserDomainRole esgDrafter = mock(UserDomainRole.class);
            when(esgDrafter.getDomain()).thenReturn(esgDomain);
            when(esgDrafter.getRole()).thenReturn(drafterRole);

            UserDomainRole safetyDrafter = mock(UserDomainRole.class);
            when(safetyDrafter.getDomain()).thenReturn(safetyDomain);
            when(safetyDrafter.getRole()).thenReturn(drafterRole);

            UserDomainRole complianceApprover = mock(UserDomainRole.class);
            when(complianceApprover.getDomain()).thenReturn(complianceDomain);
            when(complianceApprover.getRole()).thenReturn(approverRole);

            user.addDomainRole(esgDrafter);
            user.addDomainRole(safetyDrafter);
            user.addDomainRole(complianceApprover);

            // when
            List<Domain> drafterDomains = user.getDomainsWithRole("DRAFTER");
            List<Domain> approverDomains = user.getDomainsWithRole("APPROVER");
            List<Domain> reviewerDomains = user.getDomainsWithRole("REVIEWER");

            // then
            assertThat(drafterDomains).hasSize(2);
            assertThat(drafterDomains).extracting(Domain::getCode)
                    .containsExactlyInAnyOrder("ESG", "SAFETY");

            assertThat(approverDomains).hasSize(1);
            assertThat(approverDomains.get(0).getCode()).isEqualTo("COMPLIANCE");

            assertThat(reviewerDomains).isEmpty();
        }

        @Test
        @DisplayName("해당 역할을 가진 도메인이 없으면 빈 목록 반환")
        void getDomainsWithRole_NoMatchingRoles_ReturnsEmptyList() {
            // given
            UserDomainRole udr = mock(UserDomainRole.class);
            when(udr.getDomain()).thenReturn(esgDomain);
            when(udr.getRole()).thenReturn(drafterRole);
            user.addDomainRole(udr);

            // when
            List<Domain> result = user.getDomainsWithRole("REVIEWER");

            // then
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("도메인 역할이 없으면 빈 목록 반환")
        void getDomainsWithRole_NoDomainRoles_ReturnsEmptyList() {
            // when
            List<Domain> result = user.getDomainsWithRole("DRAFTER");

            // then
            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("addDomainRole() 및 removeDomainRole() 테스트")
    class DomainRoleManagementTest {

        @Test
        @DisplayName("도메인 역할 추가 성공")
        void addDomainRole_Success() {
            // given
            UserDomainRole udr = mock(UserDomainRole.class);
            when(udr.getDomain()).thenReturn(esgDomain);
            when(udr.getRole()).thenReturn(drafterRole);

            // when
            user.addDomainRole(udr);

            // then
            assertThat(user.getDomainRoles()).hasSize(1);
            assertThat(user.hasRoleInDomain("ESG", "DRAFTER")).isTrue();
        }

        @Test
        @DisplayName("여러 도메인 역할 추가 성공")
        void addMultipleDomainRoles_Success() {
            // given
            UserDomainRole esgDrafter = mock(UserDomainRole.class);
            when(esgDrafter.getDomain()).thenReturn(esgDomain);
            when(esgDrafter.getRole()).thenReturn(drafterRole);

            UserDomainRole safetyApprover = mock(UserDomainRole.class);
            when(safetyApprover.getDomain()).thenReturn(safetyDomain);
            when(safetyApprover.getRole()).thenReturn(approverRole);

            // when
            user.addDomainRole(esgDrafter);
            user.addDomainRole(safetyApprover);

            // then
            assertThat(user.getDomainRoles()).hasSize(2);
            assertThat(user.hasRoleInDomain("ESG", "DRAFTER")).isTrue();
            assertThat(user.hasRoleInDomain("SAFETY", "APPROVER")).isTrue();
        }

        @Test
        @DisplayName("도메인 역할 제거 성공")
        void removeDomainRole_Success() {
            // given
            UserDomainRole esgDrafter = mock(UserDomainRole.class);
            when(esgDrafter.getDomain()).thenReturn(esgDomain);
            when(esgDrafter.getRole()).thenReturn(drafterRole);

            UserDomainRole safetyApprover = mock(UserDomainRole.class);
            when(safetyApprover.getDomain()).thenReturn(safetyDomain);
            when(safetyApprover.getRole()).thenReturn(approverRole);

            user.addDomainRole(esgDrafter);
            user.addDomainRole(safetyApprover);

            // when
            user.removeDomainRole("ESG");

            // then
            assertThat(user.getDomainRoles()).hasSize(1);
            assertThat(user.hasRoleInDomain("ESG", "DRAFTER")).isFalse();
            assertThat(user.hasRoleInDomain("SAFETY", "APPROVER")).isTrue();
        }

        @Test
        @DisplayName("존재하지 않는 도메인 역할 제거 시 변화 없음")
        void removeDomainRole_NotExists_NoChange() {
            // given
            UserDomainRole udr = mock(UserDomainRole.class);
            when(udr.getDomain()).thenReturn(esgDomain);
            when(udr.getRole()).thenReturn(drafterRole);
            user.addDomainRole(udr);

            // when
            user.removeDomainRole("COMPLIANCE");

            // then
            assertThat(user.getDomainRoles()).hasSize(1);
            assertThat(user.hasRoleInDomain("ESG", "DRAFTER")).isTrue();
        }
    }

    @Nested
    @DisplayName("복합 시나리오 테스트")
    class ComplexScenarioTest {

        @Test
        @DisplayName("사용자가 여러 도메인에서 다양한 역할을 가진 경우")
        void userWithMultipleDomainsAndRoles() {
            // given - ESG에서 DRAFTER, SAFETY에서 APPROVER, COMPLIANCE에서 REVIEWER
            UserDomainRole esgDrafter = mock(UserDomainRole.class);
            when(esgDrafter.getDomain()).thenReturn(esgDomain);
            when(esgDrafter.getRole()).thenReturn(drafterRole);

            UserDomainRole safetyApprover = mock(UserDomainRole.class);
            when(safetyApprover.getDomain()).thenReturn(safetyDomain);
            when(safetyApprover.getRole()).thenReturn(approverRole);

            UserDomainRole complianceReviewer = mock(UserDomainRole.class);
            when(complianceReviewer.getDomain()).thenReturn(complianceDomain);
            when(complianceReviewer.getRole()).thenReturn(reviewerRole);

            user.addDomainRole(esgDrafter);
            user.addDomainRole(safetyApprover);
            user.addDomainRole(complianceReviewer);

            // then - getRoleForDomain 검증
            assertThat(user.getRoleForDomain("ESG")).isPresent();
            assertThat(user.getRoleForDomain("ESG").get().getCode()).isEqualTo("DRAFTER");
            assertThat(user.getRoleForDomain("SAFETY")).isPresent();
            assertThat(user.getRoleForDomain("SAFETY").get().getCode()).isEqualTo("APPROVER");
            assertThat(user.getRoleForDomain("COMPLIANCE")).isPresent();
            assertThat(user.getRoleForDomain("COMPLIANCE").get().getCode()).isEqualTo("REVIEWER");

            // then - hasRoleInDomain 검증
            assertThat(user.hasRoleInDomain("ESG", "DRAFTER")).isTrue();
            assertThat(user.hasRoleInDomain("ESG", "APPROVER")).isFalse();
            assertThat(user.hasRoleInDomain("SAFETY", "APPROVER")).isTrue();
            assertThat(user.hasRoleInDomain("COMPLIANCE", "REVIEWER")).isTrue();

            // then - hasAnyRoleInDomain 검증
            assertThat(user.hasAnyRoleInDomain("ESG", "DRAFTER", "APPROVER")).isTrue();
            assertThat(user.hasAnyRoleInDomain("ESG", "APPROVER", "REVIEWER")).isFalse();
            assertThat(user.hasAnyRoleInDomain("SAFETY", "DRAFTER", "APPROVER", "REVIEWER")).isTrue();

            // then - getDomainsWithRole 검증
            assertThat(user.getDomainsWithRole("DRAFTER")).hasSize(1);
            assertThat(user.getDomainsWithRole("DRAFTER").get(0).getCode()).isEqualTo("ESG");
            assertThat(user.getDomainsWithRole("APPROVER")).hasSize(1);
            assertThat(user.getDomainsWithRole("APPROVER").get(0).getCode()).isEqualTo("SAFETY");
            assertThat(user.getDomainsWithRole("REVIEWER")).hasSize(1);
            assertThat(user.getDomainsWithRole("REVIEWER").get(0).getCode()).isEqualTo("COMPLIANCE");
        }

        @Test
        @DisplayName("동일 역할로 여러 도메인에 할당된 경우")
        void userWithSameRoleAcrossMultipleDomains() {
            // given - 3개 도메인 모두에서 REVIEWER 역할
            UserDomainRole esgReviewer = mock(UserDomainRole.class);
            when(esgReviewer.getDomain()).thenReturn(esgDomain);
            when(esgReviewer.getRole()).thenReturn(reviewerRole);

            UserDomainRole safetyReviewer = mock(UserDomainRole.class);
            when(safetyReviewer.getDomain()).thenReturn(safetyDomain);
            when(safetyReviewer.getRole()).thenReturn(reviewerRole);

            UserDomainRole complianceReviewer = mock(UserDomainRole.class);
            when(complianceReviewer.getDomain()).thenReturn(complianceDomain);
            when(complianceReviewer.getRole()).thenReturn(reviewerRole);

            user.addDomainRole(esgReviewer);
            user.addDomainRole(safetyReviewer);
            user.addDomainRole(complianceReviewer);

            // then
            assertThat(user.getDomainRoles()).hasSize(3);
            assertThat(user.hasRoleInDomain("ESG", "REVIEWER")).isTrue();
            assertThat(user.hasRoleInDomain("SAFETY", "REVIEWER")).isTrue();
            assertThat(user.hasRoleInDomain("COMPLIANCE", "REVIEWER")).isTrue();

            List<Domain> reviewerDomains = user.getDomainsWithRole("REVIEWER");
            assertThat(reviewerDomains).hasSize(3);
            assertThat(reviewerDomains).extracting(Domain::getCode)
                    .containsExactlyInAnyOrder("ESG", "SAFETY", "COMPLIANCE");
        }
    }
}
