package com.smartchain.platform.domain.diagnostic.repository;

import com.smartchain.platform.domain.diagnostic.entity.Diagnostic;
import com.smartchain.platform.domain.diagnostic.entity.DiagnosticHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DiagnosticHistoryRepository extends JpaRepository<DiagnosticHistory, Long> {
    List<DiagnosticHistory> findByDiagnosticOrderByCreatedAtDesc(Diagnostic diagnostic);

    void deleteAllByDiagnostic(Diagnostic diagnostic);
}
