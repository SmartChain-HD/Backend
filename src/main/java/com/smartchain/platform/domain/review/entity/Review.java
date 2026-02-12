package com.smartchain.platform.domain.review.entity;

import com.smartchain.platform.domain.diagnostic.entity.Diagnostic;
import com.smartchain.platform.domain.user.entity.Company;
import com.smartchain.platform.domain.user.entity.Domain;
import com.smartchain.platform.domain.user.entity.User;
import com.smartchain.platform.global.entity.BaseTimeEntity;
import com.smartchain.platform.global.enums.ReviewStatus;
import com.smartchain.platform.global.enums.RiskLevel;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "review")
public class Review extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long reviewId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "diagnostic_id", nullable = false)
    private Diagnostic diagnostic;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id", nullable = false)
    private Company company;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_reviewer_id")
    private User assignedReviewer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "domain_id")
    private Domain domain;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReviewStatus status;

    private Integer score;

    @Enumerated(EnumType.STRING)
    private RiskLevel riskLevel;

    private LocalDateTime submittedAt;

    private LocalDateTime processedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "processed_by_id")
    private User processedBy;

    @Column(length = 2000)
    private String comment;

    @Column(length = 2000)
    private String categoryCommentE;

    @Column(length = 2000)
    private String categoryCommentS;

    @Column(length = 2000)
    private String categoryCommentG;

    @Builder
    public Review(Diagnostic diagnostic, Company company, User assignedReviewer, Domain domain,
                  Integer score, LocalDateTime submittedAt) {
        this.diagnostic = diagnostic;
        this.company = company;
        this.assignedReviewer = assignedReviewer;
        this.domain = domain;
        this.score = score;
        this.riskLevel = RiskLevel.fromScore(score);
        this.submittedAt = submittedAt;
        this.status = ReviewStatus.REVIEWING;
    }

    public void approve(User reviewer, String comment, String commentE, String commentS, String commentG) {
        this.status = ReviewStatus.APPROVED;
        this.processedBy = reviewer;
        this.processedAt = LocalDateTime.now();
        this.comment = comment;
        this.categoryCommentE = commentE;
        this.categoryCommentS = commentS;
        this.categoryCommentG = commentG;
    }

    public void requestRevision(User reviewer, String comment) {
        this.status = ReviewStatus.REVISION_REQUIRED;
        this.processedBy = reviewer;
        this.processedAt = LocalDateTime.now();
        this.comment = comment;
    }

    public boolean isReviewing() {
        return this.status == ReviewStatus.REVIEWING;
    }

    public boolean isRevisionRequired() {
        return this.status == ReviewStatus.REVISION_REQUIRED;
    }

    /**
     * REVISION_REQUIRED 상태에서 다시 REVIEWING 상태로 되돌림
     * 수신자가 반려 결정을 철회하고 재심사하려는 경우 사용
     */
    public void revertToReviewing() {
        this.status = ReviewStatus.REVIEWING;
        this.processedBy = null;
        this.processedAt = null;
        this.comment = null;
    }

    /**
     * 반려 후 재제출 시 기존 Review 재사용
     * REVISION_REQUIRED 상태에서 REVIEWING으로 되돌리고 제출 시간 업데이트
     */
    public void resubmit(LocalDateTime newSubmittedAt) {
        this.status = ReviewStatus.REVIEWING;
        this.submittedAt = newSubmittedAt;
        this.processedBy = null;
        this.processedAt = null;
        this.comment = null;
        this.categoryCommentE = null;
        this.categoryCommentS = null;
        this.categoryCommentG = null;
    }

    public void updateScore(Integer score) {
        this.score = score;
        this.riskLevel = RiskLevel.fromScore(score);
    }

    public void updateRiskLevel(RiskLevel riskLevel) {
        this.riskLevel = riskLevel;
    }
}
