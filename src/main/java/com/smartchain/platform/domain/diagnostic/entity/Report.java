package com.smartchain.platform.domain.diagnostic.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import jakarta.persistence.Id;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "report")
public class Report {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long reportId;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "diagnostic_id")
    private Diagnostic diagnostic;

    private String filePath; // PDF 경로
    private String status;   // 생성 상태
    private String riskLevel; // 고, 중, 저

    @CreationTimestamp
    private LocalDateTime createdAt;
}