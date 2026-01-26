package com.smartchain.platform.domain.diagnostic.entity;

import com.smartchain.platform.domain.user.entity.Domain;
import com.smartchain.platform.global.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Campaign extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long campaignId;

    @Column(unique = true)
    private String campaignCode;

    private Long ownerCompanyId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "domain_id")
    private Domain domain;

    private String title;
    private String content;

    private LocalDate periodStartDate;
    private LocalDate periodEndDate;
    private LocalDate deadline;

    @Builder
    public Campaign(String campaignCode, Long ownerCompanyId, Domain domain, String title, String content,
                    LocalDate periodStartDate, LocalDate periodEndDate, LocalDate deadline) {
        this.campaignCode = campaignCode;
        this.ownerCompanyId = ownerCompanyId;
        this.domain = domain;
        this.title = title;
        this.content = content;
        this.periodStartDate = periodStartDate;
        this.periodEndDate = periodEndDate;
        this.deadline = deadline;
    }
}