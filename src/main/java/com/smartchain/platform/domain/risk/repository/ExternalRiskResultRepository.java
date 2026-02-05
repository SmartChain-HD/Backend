package com.smartchain.platform.domain.risk.repository;

import com.smartchain.platform.domain.risk.entity.ExternalRiskResult;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ExternalRiskResultRepository extends JpaRepository<ExternalRiskResult, Long> {

    Optional<ExternalRiskResult> findTopByCompanyIdOrderByDetectedAtDesc(Long companyId);

    Page<ExternalRiskResult> findAllByOrderByDetectedAtDesc(Pageable pageable);

    Page<ExternalRiskResult> findByRequestedByOrderByDetectedAtDesc(Long requestedBy, Pageable pageable);
}
