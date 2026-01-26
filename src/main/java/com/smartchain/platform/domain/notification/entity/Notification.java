package com.smartchain.platform.domain.notification.entity;

import com.smartchain.platform.domain.user.entity.User;
import com.smartchain.platform.global.entity.BaseTimeEntity;
import com.smartchain.platform.global.enums.NotificationType;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "notification")
public class Notification extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long notificationId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private NotificationType type;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(nullable = false, length = 500)
    private String message;

    @Column(length = 500)
    private String linkUrl;

    @Column(nullable = false)
    private boolean isRead;

    @Builder
    public Notification(User user, NotificationType type, String title, String message, String linkUrl) {
        this.user = user;
        this.type = type;
        this.title = title;
        this.message = message;
        this.linkUrl = linkUrl;
        this.isRead = false;
    }

    public void markAsRead() {
        this.isRead = true;
    }
}
