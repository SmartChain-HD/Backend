package com.smartchain.platform.dto.review.detail;

import com.smartchain.platform.dto.approval.detail.RequesterDetailDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReviewDetailResponse {
    private Long reviewId;
    private String reviewIdLabel;        // "심사 ID: REVIEW-2026-0001"
    private RequesterDetailDto drafter;          // 기안자 정보
    private DiagnosticDetailInfoDto diagnostic;  // v3.0: 진단 정보 구조화
    private CompanyDetailInfoDto company;        // v3.0: 회사 정보 구조화
    private String domainCode;           // 도메인 코드 (ESG, SAFETY, COMPLIANCE)
    private String domainName;           // 도메인 이름
    private List<String> allowedCategoryKeys; // 도메인별 허용 카테고리 키 (ESG: [E,S,G], SAFETY/COMPLIANCE: [])
    private int score;                   // v3.0: 점수
    private String riskLevel;            // HIGH, MEDIUM, LOW
    private String riskLevelLabel;
    private String riskColorClass;
    private String status;
    private String statusLabel;          // 한글 상태 라벨 (검수중, 승인, 보완요청)
    private LocalDateTime submittedAt;
    
    // 파일 정보
    private List<FileInfoDto> files;
    
    // 탭 목록
    private List<ReviewDetailTabDto> tabs;
    
    // 우측 상태 카드
    private ReviewStatusCardDto statusCard;
    
    // 작업 목록
    private List<ReviewActionDto> availableActions;
    
    // 심사 이력
    private List<ReviewHistoryItemDto> reviewHistory;
}
