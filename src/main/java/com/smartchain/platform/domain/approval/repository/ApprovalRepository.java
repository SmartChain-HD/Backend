package com.smartchain.platform.domain.approval.repository;

import com.smartchain.platform.domain.approval.entity.Approval;
import com.smartchain.platform.domain.diagnostic.entity.Diagnostic;
import com.smartchain.platform.domain.user.entity.Company;
import com.smartchain.platform.domain.user.entity.Domain;
import com.smartchain.platform.global.enums.ApprovalStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ApprovalRepository extends JpaRepository<Approval, Long> {

    // Diagnostic으로 Approval 조회 (재제출 시 기존 Approval 재사용)
    Optional<Approval> findByDiagnostic(Diagnostic diagnostic);

    @Query("SELECT a FROM Approval a JOIN a.diagnostic d WHERE d.company = :company ORDER BY a.createdAt DESC")
    Page<Approval> findByCompanyOrderByCreatedAtDesc(@Param("company") Company company, Pageable pageable);

    @Query("SELECT a FROM Approval a JOIN a.diagnostic d WHERE d.company = :company AND a.status = :status ORDER BY a.createdAt DESC")
    Page<Approval> findByCompanyAndStatusOrderByCreatedAtDesc(
            @Param("company") Company company,
            @Param("status") ApprovalStatus status,
            Pageable pageable);

    Page<Approval> findByStatusOrderByCreatedAtDesc(ApprovalStatus status, Pageable pageable);

    Page<Approval> findAllByOrderByCreatedAtDesc(Pageable pageable);

    @Query("SELECT COUNT(a) FROM Approval a JOIN a.diagnostic d WHERE d.company = :company AND a.status = :status")
    int countByCompanyAndStatus(@Param("company") Company company, @Param("status") ApprovalStatus status);

    int countByStatus(ApprovalStatus status);

    // 도메인 기반 조회 (APPROVER가 해당 도메인에서 같은 회사의 결재 조회)
    @Query("SELECT a FROM Approval a JOIN a.diagnostic d WHERE d.domain IN :domains AND d.company = :company ORDER BY a.createdAt DESC")
    Page<Approval> findByDomainsAndCompanyOrderByCreatedAtDesc(
            @Param("domains") List<Domain> domains,
            @Param("company") Company company,
            Pageable pageable);

    // 도메인 + 상태 기반 조회
    @Query("SELECT a FROM Approval a JOIN a.diagnostic d WHERE d.domain IN :domains AND d.company = :company AND a.status = :status ORDER BY a.createdAt DESC")
    Page<Approval> findByDomainsAndCompanyAndStatusOrderByCreatedAtDesc(
            @Param("domains") List<Domain> domains,
            @Param("company") Company company,
            @Param("status") ApprovalStatus status,
            Pageable pageable);

    // 도메인 + 회사 기반 상태별 개수 조회
    @Query("SELECT COUNT(a) FROM Approval a JOIN a.diagnostic d WHERE d.domain IN :domains AND d.company = :company AND a.status = :status")
    int countByDomainsAndCompanyAndStatus(
            @Param("domains") List<Domain> domains,
            @Param("company") Company company,
            @Param("status") ApprovalStatus status);

    // 단일 도메인 필터 조회 (domainCode 쿼리 파라미터용)
    @Query("SELECT a FROM Approval a JOIN a.diagnostic d WHERE d.domain = :domain AND d.company = :company ORDER BY a.createdAt DESC")
    Page<Approval> findByDomainAndCompanyOrderByCreatedAtDesc(
            @Param("domain") Domain domain,
            @Param("company") Company company,
            Pageable pageable);

    // 단일 도메인 + 상태 필터 조회
    @Query("SELECT a FROM Approval a JOIN a.diagnostic d WHERE d.domain = :domain AND d.company = :company AND a.status = :status ORDER BY a.createdAt DESC")
    Page<Approval> findByDomainAndCompanyAndStatusOrderByCreatedAtDesc(
            @Param("domain") Domain domain,
            @Param("company") Company company,
            @Param("status") ApprovalStatus status,
            Pageable pageable);

    // 단일 도메인 + 회사 상태별 개수
    @Query("SELECT COUNT(a) FROM Approval a JOIN a.diagnostic d WHERE d.domain = :domain AND d.company = :company AND a.status = :status")
    int countByDomainAndCompanyAndStatus(
            @Param("domain") Domain domain,
            @Param("company") Company company,
            @Param("status") ApprovalStatus status);
}