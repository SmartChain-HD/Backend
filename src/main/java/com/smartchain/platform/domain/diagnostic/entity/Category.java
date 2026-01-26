package com.smartchain.platform.domain.diagnostic.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter @NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "category")
public class Category {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long categoryId;

    private String name; // Environment, Social, Governance
}
