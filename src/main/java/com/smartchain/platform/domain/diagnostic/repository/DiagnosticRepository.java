package com.smartchain.platform.domain.diagnostic.repository;

import com.smartchain.platform.domain.diagnostic.entity.Campaign;
import com.smartchain.platform.domain.diagnostic.entity.Diagnostic;
import com.smartchain.platform.domain.user.entity.Company;
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

    List<Diagnostic> findByCampaign(Campaign campaign);
}
