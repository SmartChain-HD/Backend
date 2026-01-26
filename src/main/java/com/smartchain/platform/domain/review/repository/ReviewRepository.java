package com.smartchain.platform.domain.review.repository;

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

public interface ReviewRepository extends JpaRepository<Review, Long> {

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

    // 담당 심사자 + 도메인별 조회
    Page<Review> findByAssignedReviewerAndDomainOrderByCreatedAtDesc(User assignedReviewer, Domain domain, Pageable pageable);

    List<Review> findByAssignedReviewerAndDomain(User assignedReviewer, Domain domain);
}
