package com.smartchain.platform.domain.user.entity;

import com.smartchain.platform.global.entity.BaseTimeEntity;
import com.smartchain.platform.global.enums.UserStatus;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

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

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<UserDomainRole> domainRoles = new ArrayList<>();

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

    /**
     * 특정 도메인에서의 역할 조회
     */
    public Optional<Role> getRoleForDomain(String domainCode) {
        return domainRoles.stream()
                .filter(udr -> udr.getDomain().getCode().equals(domainCode))
                .map(UserDomainRole::getRole)
                .findFirst();
    }

    /**
     * 특정 도메인에서 특정 역할을 가지고 있는지 확인
     */
    public boolean hasRoleInDomain(String domainCode, String roleCode) {
        return domainRoles.stream()
                .anyMatch(udr ->
                        udr.getDomain().getCode().equals(domainCode) &&
                        udr.getRole().getCode().equals(roleCode));
    }

    /**
     * 특정 도메인에서 주어진 역할들 중 하나라도 가지고 있는지 확인
     */
    public boolean hasAnyRoleInDomain(String domainCode, String... roleCodes) {
        return domainRoles.stream()
                .anyMatch(udr ->
                        udr.getDomain().getCode().equals(domainCode) &&
                        java.util.Arrays.asList(roleCodes).contains(udr.getRole().getCode()));
    }

    /**
     * 도메인별 역할 추가
     */
    public void addDomainRole(UserDomainRole domainRole) {
        this.domainRoles.add(domainRole);
    }

    /**
     * 특정 도메인의 역할 제거
     */
    public void removeDomainRole(String domainCode) {
        this.domainRoles.removeIf(udr -> udr.getDomain().getCode().equals(domainCode));
    }
}
