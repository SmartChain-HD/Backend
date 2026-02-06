package com.smartchain.platform.domain.approval.entity;

import com.smartchain.platform.domain.diagnostic.entity.Diagnostic;
import com.smartchain.platform.domain.user.entity.User;
import com.smartchain.platform.global.entity.BaseTimeEntity;
import com.smartchain.platform.global.enums.ApprovalStatus;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Approval extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long approvalId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "diagnostic_id", nullable = false)
    private Diagnostic diagnostic;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "requester_id", nullable = false)
    private User requester;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "approver_id")
    private User approver;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ApprovalStatus status;

    private String requestComment;

    private String approverComment;

    private LocalDate deadline;

    private LocalDateTime processedAt;

    private LocalDateTime submittedToReviewerAt;

    @Builder
    public Approval(Diagnostic diagnostic, User requester, String requestComment, LocalDate deadline) {
        this.diagnostic = diagnostic;
        this.requester = requester;
        this.requestComment = requestComment;
        this.deadline = deadline;
        this.status = ApprovalStatus.WAITING;
    }

    public void approve(User approver, String comment) {
        this.status = ApprovalStatus.APPROVED;
        this.approver = approver;
        this.approverComment = comment;
        this.processedAt = LocalDateTime.now();
    }

    public void reject(User approver, String comment) {
        this.status = ApprovalStatus.REJECTED;
        this.approver = approver;
        this.approverComment = comment;
        this.processedAt = LocalDateTime.now();
    }

    public void submitToReviewer() {
        this.submittedToReviewerAt = LocalDateTime.now();
    }

    public boolean isWaiting() {
        return this.status == ApprovalStatus.WAITING;
    }

    public boolean isApproved() {
        return this.status == ApprovalStatus.APPROVED;
    }

    /**
     * 반려 후 재제출 시 기존 Approval 재사용
     * REJECTED 상태에서 WAITING으로 되돌리고 결재 정보 초기화
     */
    public void resubmit(User requester, String requestComment) {
        this.status = ApprovalStatus.WAITING;
        this.requester = requester;
        this.requestComment = requestComment;
        this.approver = null;
        this.approverComment = null;
        this.processedAt = null;
        this.submittedToReviewerAt = null;
    }
}
