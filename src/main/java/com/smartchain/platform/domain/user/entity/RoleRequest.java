package com.smartchain.platform.domain.user.entity;

import com.smartchain.platform.global.enums.RequestStatus;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import jakarta.persistence.Id;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "role_request")
public class RoleRequest {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long requestId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id")
    private Company company;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "processed_by")
    private User processedBy;

    private String requestedRole;
    private String reason;

    @Enumerated(EnumType.STRING)
    private RequestStatus status;

    private String rejectReason;

    private LocalDateTime decidedAt;
    @CreationTimestamp
    private LocalDateTime createdAt;

    @Builder
    public RoleRequest(User user, Company company, String requestedRole, String reason, RequestStatus status) {
        this.user = user;
        this.company = company;
        this.requestedRole = requestedRole;
        this.reason = reason;
        this.status = status;
    }

    public void approve(User processor) {
        this.status = RequestStatus.APPROVED;
        this.processedBy = processor;
        this.decidedAt = LocalDateTime.now();
    }

    public void reject(User processor, String rejectReason) {
        this.status = RequestStatus.REJECTED;
        this.processedBy = processor;
        this.rejectReason = rejectReason;
        this.decidedAt = LocalDateTime.now();
    }

    public boolean isPending() {
        return this.status == RequestStatus.PENDING;
    }
}