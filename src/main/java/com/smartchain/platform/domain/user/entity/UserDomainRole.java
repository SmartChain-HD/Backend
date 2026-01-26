package com.smartchain.platform.domain.user.entity;

import com.smartchain.platform.global.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "user_domain_role",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_user_domain",
                columnNames = {"user_id", "domain_id"}
        ))
public class UserDomainRole extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_domain_role_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "domain_id", nullable = false)
    private Domain domain;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "role_id", nullable = false)
    private Role role;

    @Builder
    public UserDomainRole(User user, Domain domain, Role role) {
        this.user = user;
        this.domain = domain;
        this.role = role;
    }

    public void changeRole(Role newRole) {
        this.role = newRole;
    }
}
