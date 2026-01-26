package com.smartchain.platform.domain.review.entity;

import com.smartchain.platform.domain.diagnostic.entity.Diagnostic;
import com.smartchain.platform.domain.user.entity.Company;
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

    @Column(length = 1000)
    private String comment;

    @Column(length = 500)
    private String categoryCommentE;

    @Column(length = 500)
    private String categoryCommentS;

    @Column(length = 500)
    private String categoryCommentG;

    @Builder
    public Review(Diagnostic diagnostic, Company company, User assignedReviewer,
                  Integer score, LocalDateTime submittedAt) {
        this.diagnostic = diagnostic;
        this.company = company;
        this.assignedReviewer = assignedReviewer;
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

    public void updateScore(Integer score) {
        this.score = score;
        this.riskLevel = RiskLevel.fromScore(score);
    }
}
