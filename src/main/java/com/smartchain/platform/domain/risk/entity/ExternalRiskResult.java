package com.smartchain.platform.domain.risk.entity;

import com.smartchain.platform.global.entity.BaseTimeEntity;
import com.smartchain.platform.global.enums.RiskLevel;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.time.ZoneId;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "external_risk_result")
public class ExternalRiskResult extends BaseTimeEntity {
    private static final ZoneId KST_ZONE = ZoneId.of("Asia/Seoul");

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long companyId;

    @Column(nullable = false)
    private String companyName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RiskLevel riskLevel;

    @Column(columnDefinition = "TEXT")
    private String summary;

    @Column(columnDefinition = "TEXT")
    private String evidenceJson;

    @Column(nullable = false)
    private Long requestedBy;

    @Column(nullable = false)
    private LocalDateTime detectedAt;

    @Builder
    public ExternalRiskResult(Long companyId, String companyName, RiskLevel riskLevel,
                              String summary, String evidenceJson, Long requestedBy) {
        this.companyId = companyId;
        this.companyName = companyName;
        this.riskLevel = riskLevel;
        this.summary = summary;
        this.evidenceJson = evidenceJson;
        this.requestedBy = requestedBy;
        this.detectedAt = LocalDateTime.now(KST_ZONE);
    }
}
