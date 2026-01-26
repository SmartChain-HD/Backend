package com.smartchain.platform.domain.user.entity;

import com.smartchain.platform.global.entity.BaseTimeEntity;
import com.smartchain.platform.global.enums.UserStatus;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "\"User\"") // PostgreSQL/H2 예약어 이슈 방지
public class User extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long userId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id")
    private Company company;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "role_id")
    private Role role;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String userPassword;

    private LocalDateTime lastLoginAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserStatus status = UserStatus.ACTIVE;

    @Builder
    public User(String name, String email, String userPassword, Company company, Role role) {
        this.name = name;
        this.email = email;
        this.userPassword = userPassword;
        this.company = company;
        this.role = role;
        this.status = UserStatus.ACTIVE;
    }

    public void updateLastLoginAt() {
        this.lastLoginAt = LocalDateTime.now();
    }

    public void changeRole(Role newRole) {
        this.role = newRole;
    }

    public void changeStatus(UserStatus newStatus) {
        this.status = newStatus;
    }
}
