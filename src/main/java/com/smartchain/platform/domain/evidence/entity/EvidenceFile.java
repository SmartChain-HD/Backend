package com.smartchain.platform.domain.evidence.entity;

import com.smartchain.platform.domain.diagnostic.entity.Diagnostic;
import com.smartchain.platform.domain.diagnostic.entity.ResultQuant;
import com.smartchain.platform.global.entity.BaseTimeEntity;
import com.smartchain.platform.global.enums.ParsingStatus;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class EvidenceFile extends BaseTimeEntity { // uploadedAt -> createdAt 사용
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long resultFileId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "result_id")
    private ResultQuant resultQuant;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "diagnostic_id")
    private Diagnostic diagnostic;

    private String filePath;
    private String originalFileName;
    private Long fileSize;
    private String mimeType;

    @Enumerated(EnumType.STRING)
    private ParsingStatus parsingStatus;

    private BigDecimal valNum;
    private String valText;

    @Column(columnDefinition = "TEXT") // 혹은 JSON
    private String parsingMetaInfo;

    @Builder
    public EvidenceFile(Diagnostic diagnostic, ResultQuant resultQuant, String filePath,
                        String originalFileName, Long fileSize, String mimeType) {
        this.diagnostic = diagnostic;
        this.resultQuant = resultQuant;
        this.filePath = filePath;
        this.originalFileName = originalFileName;
        this.fileSize = fileSize;
        this.mimeType = mimeType;
        this.parsingStatus = ParsingStatus.WAITING;
    }

    public void startParsing() {
        this.parsingStatus = ParsingStatus.PROCESSING;
    }

    public void completeParsing(BigDecimal valNum, String valText, String metaInfo) {
        this.parsingStatus = ParsingStatus.SUCCESS;
        this.valNum = valNum;
        this.valText = valText;
        this.parsingMetaInfo = metaInfo;
    }

    public void failParsing(String errorInfo) {
        this.parsingStatus = ParsingStatus.FAILED;
        this.parsingMetaInfo = errorInfo;
    }
}
