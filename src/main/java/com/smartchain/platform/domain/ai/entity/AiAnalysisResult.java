package com.smartchain.platform.domain.ai.entity;

import com.smartchain.platform.domain.diagnostic.entity.Diagnostic;
import com.smartchain.platform.global.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * AI 분석 결과 엔티티
 */
@Entity
@Table(name = "ai_analysis_result")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AiAnalysisResult extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "diagnostic_id", nullable = false)
    private Diagnostic diagnostic;

    @Column(name = "domain_code", nullable = false)
    private String domainCode;  // ESG, COMPLIANCE, SAFETY

    @Column(name = "package_id")
    private String packageId;  // AI Run API의 package_id

    @Column(name = "risk_level")
    private String riskLevel;  // LOW, MEDIUM, HIGH

    @Column(nullable = false)
    private String verdict;  // PASS, WARN, NEED_CLARIFY, NEED_FIX

    @Column(name = "why_summary", length = 500)
    private String whySummary;

    @Column(name = "result_json", columnDefinition = "TEXT")
    private String resultJson;  // 전체 응답 JSON

    @Column(name = "analyzed_at")
    private LocalDateTime analyzedAt;

    @Builder
    public AiAnalysisResult(Diagnostic diagnostic, String domainCode, String packageId,
                            String riskLevel, String verdict, String whySummary,
                            String resultJson, LocalDateTime analyzedAt) {
        this.diagnostic = diagnostic;
        this.domainCode = domainCode;
        this.packageId = packageId;
        this.riskLevel = riskLevel;
        this.verdict = verdict;
        this.whySummary = whySummary;
        this.resultJson = resultJson;
        this.analyzedAt = analyzedAt != null ? analyzedAt : LocalDateTime.now();
    }
}
