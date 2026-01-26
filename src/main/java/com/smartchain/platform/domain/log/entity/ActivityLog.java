package com.smartchain.platform.domain.log.entity;

import com.smartchain.platform.domain.user.entity.User;
import com.smartchain.platform.global.enums.ActionType;
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
@Table(name = "activity_log")
public class ActivityLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long logId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ActionType action;

    private String status;

    private String ipAddress;

    private String userAgent;

    private String targetType;  // DIAGNOSTIC, FILE, USER 등

    private String targetId;

    @Column(length = 500)
    private String description;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @Builder
    public ActivityLog(User user, ActionType action, String status, String ipAddress,
                       String userAgent, String targetType, String targetId, String description) {
        this.user = user;
        this.action = action;
        this.status = status;
        this.ipAddress = ipAddress;
        this.userAgent = userAgent;
        this.targetType = targetType;
        this.targetId = targetId;
        this.description = description;
    }

    public Long getUserId() {
        return user != null ? user.getUserId() : null;
    }
}
