package com.smartchain.platform.domain.diagnostic.repository;

import com.smartchain.platform.domain.diagnostic.entity.ResultQuant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ResultQuantRepository extends JpaRepository<ResultQuant, Long> {

    void deleteAllByDiagnostic_DiagnosticId(Long diagnosticId);
}
