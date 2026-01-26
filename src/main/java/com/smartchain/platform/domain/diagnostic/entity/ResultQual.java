package com.smartchain.platform.domain.diagnostic.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import jakarta.persistence.Id;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "result_qual")
public class ResultQual {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long resultQualId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "diagnostic_id")
    private Diagnostic diagnostic;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "question_id")
    private Question question;

    private Boolean answer; // 예/아니오
}