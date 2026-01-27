package com.smartchain.platform.domain.diagnostic.repository;

import com.smartchain.platform.domain.diagnostic.entity.Diagnostic;
import com.smartchain.platform.domain.user.entity.Company;
import com.smartchain.platform.domain.user.entity.Domain;
import com.smartchain.platform.global.enums.DiagnosticStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface DiagnosticRepository extends JpaRepository<Diagnostic, Long> {

    Optional<Diagnostic> findByDiagnosticCode(String diagnosticCode);

    Page<Diagnostic> findByCompanyOrderByCreatedAtDesc(Company company, Pageable pageable);

    Page<Diagnostic> findByDrafterIdOrderByCreatedAtDesc(Long drafterId, Pageable pageable);

    @Query("SELECT d FROM Diagnostic d WHERE d.company = :company AND d.status IN :statuses ORDER BY d.createdAt DESC")
    Page<Diagnostic> findByCompanyAndStatusInOrderByCreatedAtDesc(
            @Param("company") Company company,
            @Param("statuses") List<DiagnosticStatus> statuses,
            Pageable pageable);

    @Query("SELECT d FROM Diagnostic d WHERE d.drafterId = :drafterId AND d.status IN :statuses ORDER BY d.createdAt DESC")
    Page<Diagnostic> findByDrafterIdAndStatusInOrderByCreatedAtDesc(
            @Param("drafterId") Long drafterId,
            @Param("statuses") List<DiagnosticStatus> statuses,
            Pageable pageable);

    @Query("SELECT d FROM Diagnostic d WHERE d.company = :company AND d.deadline BETWEEN :fromDate AND :toDate ORDER BY d.createdAt DESC")
    Page<Diagnostic> findByCompanyAndDeadlineBetweenOrderByCreatedAtDesc(
            @Param("company") Company company,
            @Param("fromDate") LocalDate fromDate,
            @Param("toDate") LocalDate toDate,
            Pageable pageable);

    @Query("SELECT COUNT(d) FROM Diagnostic d WHERE d.company = :company")
    long countByCompany(@Param("company") Company company);

    @Query("SELECT MAX(d.diagnosticId) FROM Diagnostic d")
    Long findMaxDiagnosticId();

    Page<Diagnostic> findByDomainOrderByCreatedAtDesc(Domain domain, Pageable pageable);

    List<Diagnostic> findByDomain(Domain domain);

    // 도메인 기반 권한 조회 (DRAFTER: 본인 기안만)
    @Query("SELECT d FROM Diagnostic d WHERE d.domain IN :domains AND d.drafterId = :drafterId ORDER BY d.createdAt DESC")
    Page<Diagnostic> findByDomainInAndDrafterIdOrderByCreatedAtDesc(
            @Param("domains") List<Domain> domains,
            @Param("drafterId") Long drafterId,
            Pageable pageable);

    // 도메인 기반 권한 조회 (APPROVER: 같은 회사 기안)
    @Query("SELECT d FROM Diagnostic d WHERE d.domain IN :domains AND d.company = :company ORDER BY d.createdAt DESC")
    Page<Diagnostic> findByDomainInAndCompanyOrderByCreatedAtDesc(
            @Param("domains") List<Domain> domains,
            @Param("company") Company company,
            Pageable pageable);

    // 도메인 기반 권한 조회 (REVIEWER: 해당 도메인 모든 기안)
    @Query("SELECT d FROM Diagnostic d WHERE d.domain IN :domains ORDER BY d.createdAt DESC")
    Page<Diagnostic> findByDomainInOrderByCreatedAtDesc(
            @Param("domains") List<Domain> domains,
            Pageable pageable);

    // 도메인 기반 복합 조회 (여러 역할의 도메인을 한번에 처리)
    @Query("SELECT DISTINCT d FROM Diagnostic d WHERE " +
           "(d.domain IN :reviewerDomains) OR " +
           "(d.domain IN :approverDomains AND d.company = :company) OR " +
           "(d.domain IN :drafterDomains AND d.drafterId = :drafterId) " +
           "ORDER BY d.createdAt DESC")
    Page<Diagnostic> findByUserDomainRoles(
            @Param("reviewerDomains") List<Domain> reviewerDomains,
            @Param("approverDomains") List<Domain> approverDomains,
            @Param("drafterDomains") List<Domain> drafterDomains,
            @Param("company") Company company,
            @Param("drafterId") Long drafterId,
            Pageable pageable);

    // 상태 필터 포함 도메인 기반 복합 조회
    @Query("SELECT DISTINCT d FROM Diagnostic d WHERE " +
           "d.status IN :statuses AND (" +
           "(d.domain IN :reviewerDomains) OR " +
           "(d.domain IN :approverDomains AND d.company = :company) OR " +
           "(d.domain IN :drafterDomains AND d.drafterId = :drafterId)) " +
           "ORDER BY d.createdAt DESC")
    Page<Diagnostic> findByUserDomainRolesAndStatusIn(
            @Param("reviewerDomains") List<Domain> reviewerDomains,
            @Param("approverDomains") List<Domain> approverDomains,
            @Param("drafterDomains") List<Domain> drafterDomains,
            @Param("company") Company company,
            @Param("drafterId") Long drafterId,
            @Param("statuses") List<DiagnosticStatus> statuses,
            Pageable pageable);

    // 기한 필터 포함 도메인 기반 복합 조회
    @Query("SELECT DISTINCT d FROM Diagnostic d WHERE " +
           "d.deadline BETWEEN :fromDate AND :toDate AND (" +
           "(d.domain IN :reviewerDomains) OR " +
           "(d.domain IN :approverDomains AND d.company = :company) OR " +
           "(d.domain IN :drafterDomains AND d.drafterId = :drafterId)) " +
           "ORDER BY d.createdAt DESC")
    Page<Diagnostic> findByUserDomainRolesAndDeadlineBetween(
            @Param("reviewerDomains") List<Domain> reviewerDomains,
            @Param("approverDomains") List<Domain> approverDomains,
            @Param("drafterDomains") List<Domain> drafterDomains,
            @Param("company") Company company,
            @Param("drafterId") Long drafterId,
            @Param("fromDate") LocalDate fromDate,
            @Param("toDate") LocalDate toDate,
            Pageable pageable);
}
