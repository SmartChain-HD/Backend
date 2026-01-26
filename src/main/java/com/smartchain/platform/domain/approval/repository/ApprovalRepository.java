package com.smartchain.platform.domain.approval.repository;

import com.smartchain.platform.domain.approval.entity.Approval;
import com.smartchain.platform.domain.user.entity.Company;
import com.smartchain.platform.global.enums.ApprovalStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ApprovalRepository extends JpaRepository<Approval, Long> {

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
}