package com.smartchain.platform.domain.diagnostic.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import jakarta.persistence.Id;

import java.math.BigDecimal;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "result_quant")
public class ResultQuant {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long resultId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "diagnostic_id")
    private Diagnostic diagnostic;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "question_id")
    private Question question;

    private BigDecimal valNum;
    private String valText;
}