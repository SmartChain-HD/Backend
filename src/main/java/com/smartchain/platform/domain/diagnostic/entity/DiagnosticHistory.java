package com.smartchain.platform.domain.diagnostic.entity;

import com.smartchain.platform.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "diagnostic_history")
public class DiagnosticHistory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long historyId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "diagnostic_id", nullable = false)
    private Diagnostic diagnostic;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "actor_id")
    private User actor;

    private String action;  // CREATED, SUBMITTED, APPROVED, REJECTED, etc.
    private String previousStatus;
    private String newStatus;
    private String comment;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @Builder
    public DiagnosticHistory(Diagnostic diagnostic, User actor, String action,
                             String previousStatus, String newStatus, String comment) {
        this.diagnostic = diagnostic;
        this.actor = actor;
        this.action = action;
        this.previousStatus = previousStatus;
        this.newStatus = newStatus;
        this.comment = comment;
    }
}