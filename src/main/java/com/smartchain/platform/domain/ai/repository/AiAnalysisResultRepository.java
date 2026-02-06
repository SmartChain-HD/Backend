package com.smartchain.platform.domain.ai.repository;

import com.smartchain.platform.domain.ai.entity.AiAnalysisResult;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AiAnalysisResultRepository extends JpaRepository<AiAnalysisResult, Long> {

    /**
     * 진단의 최신 분석 결과 조회
     */
    Optional<AiAnalysisResult> findTopByDiagnostic_DiagnosticIdOrderByAnalyzedAtDesc(Long diagnosticId);

    /**
     * 진단의 분석 이력 조회
     */
    List<AiAnalysisResult> findByDiagnostic_DiagnosticIdOrderByAnalyzedAtDesc(Long diagnosticId);

    /**
     * package_id로 조회
     */
    Optional<AiAnalysisResult> findByPackageId(String packageId);

    /**
     * 도메인별 분석 결과 조회
     */
    List<AiAnalysisResult> findByDomainCodeOrderByAnalyzedAtDesc(String domainCode);

    void deleteAllByDiagnostic_DiagnosticId(Long diagnosticId);
}
