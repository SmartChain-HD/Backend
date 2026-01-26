package com.smartchain.platform.domain.evidence.repository;

import com.smartchain.platform.domain.evidence.entity.EvidenceFile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EvidenceFileRepository extends JpaRepository<EvidenceFile, Long> {

    @Query("SELECT ef FROM EvidenceFile ef WHERE ef.diagnostic.diagnosticId = :diagnosticId")
    List<EvidenceFile> findByDiagnosticId(@Param("diagnosticId") Long diagnosticId);

    @Query("SELECT ef FROM EvidenceFile ef WHERE ef.resultFileId = :fileId AND ef.diagnostic.diagnosticId = :diagnosticId")
    Optional<EvidenceFile> findByIdAndDiagnosticId(@Param("fileId") Long fileId, @Param("diagnosticId") Long diagnosticId);

    @Query("SELECT COUNT(ef) FROM EvidenceFile ef WHERE ef.diagnostic.diagnosticId = :diagnosticId")
    int countByDiagnosticId(@Param("diagnosticId") Long diagnosticId);
}
