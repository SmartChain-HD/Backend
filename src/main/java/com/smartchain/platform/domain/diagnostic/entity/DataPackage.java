package com.smartchain.platform.domain.diagnostic.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import jakarta.persistence.Id;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "data_package")
public class DataPackage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "data_package")
    private Long dataPackageId;

    private Long diagnosticId;
    private Long createdBy;

    private String filePath;

    @Column(columnDefinition = "json")
    private String manifest;

    private Long fileSize;

    @CreationTimestamp
    private LocalDateTime createdAt;
}
