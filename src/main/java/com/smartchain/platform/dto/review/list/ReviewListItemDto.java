package com.smartchain.platform.dto.review.list;

import com.smartchain.platform.dto.review.common.AssignedToDto;
import com.smartchain.platform.dto.review.common.CompanySimpleDto;
import com.smartchain.platform.dto.review.common.DiagnosticSimpleDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReviewListItemDto {
    private Long reviewId;               // v3.0: auditId → reviewId (API 명세 통일)
    private String reviewIdLabel;        // "Review0001"
    private DiagnosticSimpleDto diagnostic;  // v3.0: 진단 정보 구조화
    private CompanySimpleDto company;    // v3.0: 회사 정보 구조화
    private String domainCode;           // 도메인 코드 (ESG, SAFETY, COMPLIANCE)
    private String domainName;           // 도메인 이름
    private int score;                   // v3.0: 점수 추가
    private String riskLevel;            // HIGH, MEDIUM, LOW
    private String riskLevelLabel;       // "저위험군", "중위험군", "고위험군"
    private String riskColorClass;       // CSS 클래스 (green/yellow/red)
    private String status;               // REVIEWING, APPROVED, REVISION_REQUIRED
    private String statusLabel;          // "검수중", "승인", "보완요청"
    private LocalDateTime submittedAt;
    private ReviewFileLinksDto files;    // 파일 다운로드 링크
    private AssignedToDto assignedTo;    // v3.0: 담당자 정보 추가
}
