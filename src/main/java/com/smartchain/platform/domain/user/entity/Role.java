package com.smartchain.platform.domain.user.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "role")
public class Role {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "role_id")
    private Long roleId;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true)
    private String code;

    public Role(String name, String code) {
        this.name = name;
        this.code = code;
    }
}
