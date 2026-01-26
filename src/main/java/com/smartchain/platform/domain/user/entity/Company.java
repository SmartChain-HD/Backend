package com.smartchain.platform.domain.user.entity;

import com.smartchain.platform.global.entity.BaseTimeEntity;
import com.smartchain.platform.global.enums.CompanyStatus;
import com.smartchain.platform.global.enums.CompanyType;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "company")
public class Company extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long companyId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "industry_id")
    private Industry industry;

    @Column(nullable = false)
    private String name;

    private String scale; // 대기업, 중견기업 등

    private Long sales;   // 매출액

    private Long asset;   // 자산

    @Column(unique = true)
    private String businessNumber; // 사업자등록번호

    @Enumerated(EnumType.STRING)
    private CompanyType companyType; // TIER1, TIER2

    private String ceoName; // 대표자명

    private String address; // 주소

    private String contactEmail; // 연락처 이메일

    private String contactPhone; // 연락처 전화번호

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CompanyStatus status = CompanyStatus.ACTIVE; // 상태

    @Builder
    public Company(Industry industry, String name, String scale, Long sales, Long asset,
                   String businessNumber, CompanyType companyType, String ceoName,
                   String address, String contactEmail, String contactPhone, CompanyStatus status) {
        this.industry = industry;
        this.name = name;
        this.scale = scale;
        this.sales = sales;
        this.asset = asset;
        this.businessNumber = businessNumber;
        this.companyType = companyType;
        this.ceoName = ceoName;
        this.address = address;
        this.contactEmail = contactEmail;
        this.contactPhone = contactPhone;
        this.status = status != null ? status : CompanyStatus.ACTIVE;
    }

    public void changeStatus(CompanyStatus status) {
        this.status = status;
    }
}
