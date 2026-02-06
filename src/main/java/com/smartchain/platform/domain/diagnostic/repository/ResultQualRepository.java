package com.smartchain.platform.domain.diagnostic.repository;

import com.smartchain.platform.domain.diagnostic.entity.ResultQual;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ResultQualRepository extends JpaRepository<ResultQual, Long> {

    void deleteAllByDiagnostic_DiagnosticId(Long diagnosticId);
}
