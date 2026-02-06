package com.smartchain.platform.domain.review.repository;

import com.smartchain.platform.domain.diagnostic.entity.Diagnostic;
import com.smartchain.platform.domain.review.entity.Review;
import com.smartchain.platform.domain.user.entity.Company;
import com.smartchain.platform.domain.user.entity.Domain;
import com.smartchain.platform.domain.user.entity.User;
import com.smartchain.platform.global.enums.ReviewStatus;
import com.smartchain.platform.global.enums.RiskLevel;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ReviewRepository extends JpaRepository<Review, Long> {

    // Diagnostic으로 Review 조회 (재제출 시 기존 Review 재사용)
    Optional<Review> findByDiagnostic(Diagnostic diagnostic);

    // 전체 심사 목록 조회 (REVIEWER용)
    Page<Review> findAllByOrderByCreatedAtDesc(Pageable pageable);

    // 상태별 조회
    Page<Review> findByStatusOrderByCreatedAtDesc(ReviewStatus status, Pageable pageable);

    // 위험군별 조회
    Page<Review> findByRiskLevelOrderByCreatedAtDesc(RiskLevel riskLevel, Pageable pageable);

    // 회사별 조회
    Page<Review> findByCompanyOrderByCreatedAtDesc(Company company, Pageable pageable);

    // 상태 + 위험군 필터
    Page<Review> findByStatusAndRiskLevelOrderByCreatedAtDesc(ReviewStatus status, RiskLevel riskLevel, Pageable pageable);

    // 상태 + 회사 필터
    Page<Review> findByStatusAndCompanyOrderByCreatedAtDesc(ReviewStatus status, Company company, Pageable pageable);

    // 위험군 + 회사 필터
    Page<Review> findByRiskLevelAndCompanyOrderByCreatedAtDesc(RiskLevel riskLevel, Company company, Pageable pageable);

    // 상태 + 위험군 + 회사 필터
    Page<Review> findByStatusAndRiskLevelAndCompanyOrderByCreatedAtDesc(
            ReviewStatus status, RiskLevel riskLevel, Company company, Pageable pageable);

    // 대시보드 통계용 카운트 쿼리
    long countByStatus(ReviewStatus status);

    long countByRiskLevel(RiskLevel riskLevel);

    // 전체 협력사 수 (진단 제출한 협력사)
    @Query("SELECT COUNT(DISTINCT r.company) FROM Review r")
    long countDistinctCompanies();

    // 최근 활동 조회 (상위 N개)
    @Query("SELECT r FROM Review r ORDER BY r.submittedAt DESC")
    List<Review> findTopNByOrderBySubmittedAtDesc(Pageable pageable);

    // 도메인별 조회
    List<Review> findByDomain(Domain domain);

    Page<Review> findByDomainOrderByCreatedAtDesc(Domain domain, Pageable pageable);

    // 담당 수신자 + 도메인별 조회
    Page<Review> findByAssignedReviewerAndDomainOrderByCreatedAtDesc(User assignedReviewer, Domain domain, Pageable pageable);

    List<Review> findByAssignedReviewerAndDomain(User assignedReviewer, Domain domain);

    // 도메인 목록(IN)으로 필터링
    Page<Review> findByDomainInOrderByCreatedAtDesc(List<Domain> domains, Pageable pageable);

    Page<Review> findByDomainInAndStatusOrderByCreatedAtDesc(List<Domain> domains, ReviewStatus status, Pageable pageable);

    Page<Review> findByDomainInAndRiskLevelOrderByCreatedAtDesc(List<Domain> domains, RiskLevel riskLevel, Pageable pageable);

    Page<Review> findByDomainInAndStatusAndRiskLevelOrderByCreatedAtDesc(
            List<Domain> domains, ReviewStatus status, RiskLevel riskLevel, Pageable pageable);

    Page<Review> findByDomainInAndStatusAndRiskLevelAndCompanyOrderByCreatedAtDesc(
            List<Domain> domains, ReviewStatus status, RiskLevel riskLevel, Company company, Pageable pageable);

    Page<Review> findByDomainInAndStatusAndCompanyOrderByCreatedAtDesc(
            List<Domain> domains, ReviewStatus status, Company company, Pageable pageable);

    Page<Review> findByDomainInAndRiskLevelAndCompanyOrderByCreatedAtDesc(
            List<Domain> domains, RiskLevel riskLevel, Company company, Pageable pageable);

    Page<Review> findByDomainInAndCompanyOrderByCreatedAtDesc(List<Domain> domains, Company company, Pageable pageable);

    // 도메인 목록 기반 통계
    long countByDomainIn(List<Domain> domains);

    long countByDomainInAndStatus(List<Domain> domains, ReviewStatus status);

    long countByDomainInAndRiskLevel(List<Domain> domains, RiskLevel riskLevel);

    @Query("SELECT COUNT(DISTINCT r.company) FROM Review r WHERE r.domain IN :domains")
    long countDistinctCompaniesByDomainIn(@Param("domains") List<Domain> domains);

    @Query("SELECT r FROM Review r WHERE r.domain IN :domains ORDER BY r.submittedAt DESC")
    List<Review> findByDomainInOrderBySubmittedAtDesc(@Param("domains") List<Domain> domains, Pageable pageable);

    // 단일 도메인 필터 조회 (domainCode 쿼리 파라미터용)
    Page<Review> findByDomainAndStatusOrderByCreatedAtDesc(Domain domain, ReviewStatus status, Pageable pageable);

    Page<Review> findByDomainAndRiskLevelOrderByCreatedAtDesc(Domain domain, RiskLevel riskLevel, Pageable pageable);

    Page<Review> findByDomainAndStatusAndRiskLevelOrderByCreatedAtDesc(
            Domain domain, ReviewStatus status, RiskLevel riskLevel, Pageable pageable);

    Page<Review> findByDomainAndStatusAndRiskLevelAndCompanyOrderByCreatedAtDesc(
            Domain domain, ReviewStatus status, RiskLevel riskLevel, Company company, Pageable pageable);

    Page<Review> findByDomainAndStatusAndCompanyOrderByCreatedAtDesc(
            Domain domain, ReviewStatus status, Company company, Pageable pageable);

    Page<Review> findByDomainAndRiskLevelAndCompanyOrderByCreatedAtDesc(
            Domain domain, RiskLevel riskLevel, Company company, Pageable pageable);

    Page<Review> findByDomainAndCompanyOrderByCreatedAtDesc(Domain domain, Company company, Pageable pageable);

    // 단일 도메인 통계
    long countByDomain(Domain domain);

    long countByDomainAndStatus(Domain domain, ReviewStatus status);

    long countByDomainAndRiskLevel(Domain domain, RiskLevel riskLevel);

    @Query("SELECT COUNT(DISTINCT r.company) FROM Review r WHERE r.domain = :domain")
    long countDistinctCompaniesByDomain(@Param("domain") Domain domain);

    @Query("SELECT r FROM Review r WHERE r.domain = :domain ORDER BY r.submittedAt DESC")
    List<Review> findByDomainOrderBySubmittedAtDesc(@Param("domain") Domain domain, Pageable pageable);
}
