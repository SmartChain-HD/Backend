package com.smartchain.platform.domain.user.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter @NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "industry")
public class Industry {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long industryId;

    private String name; // 제조업, IT 등
    private String code;

    public Industry(String name, String code) {
        this.name = name;
        this.code = code;
    }
}
