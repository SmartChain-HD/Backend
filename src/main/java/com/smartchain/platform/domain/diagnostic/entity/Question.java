package com.smartchain.platform.domain.diagnostic.entity;

import com.smartchain.platform.domain.user.entity.Industry;
import com.smartchain.platform.global.enums.InputType;
import com.smartchain.platform.global.enums.QuestionType;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import jakarta.persistence.Id;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "question")
public class Question {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long questionId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "industry_id")
    private Industry industry; // Null이면 공통 문항

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private Category category;

    private String content;

    @Enumerated(EnumType.STRING)
    private QuestionType questionType; // QUAL, QUANT

    @Enumerated(EnumType.STRING)
    private InputType inputType; // TEXT, RADIO, FILE

    private Boolean isActive;
    private String unit; // 정량일 때 단위
}