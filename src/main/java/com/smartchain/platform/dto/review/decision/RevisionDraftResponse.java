package com.smartchain.platform.dto.review.decision;

import com.smartchain.platform.dto.ai.run.Clarification;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.List;

/**
 * AI 보완요청 초안 응답 DTO
 * Review 상세 화면에서 보완요청 작성 시 AI가 생성한 초안을 제공
 */
@Builder
public record RevisionDraftResponse(
    Long reviewId,
    Long diagnosticId,
    boolean hasDraft,
    String riskLevel,
    String verdict,
    String draftComment,
    List<RevisionDraftItemDto> draftItems,
    LocalDateTime analyzedAt
) {
    /**
     * AI 분석 결과가 없을 때 빈 응답 생성
     */
    public static RevisionDraftResponse empty(Long reviewId, Long diagnosticId) {
        return RevisionDraftResponse.builder()
            .reviewId(reviewId)
            .diagnosticId(diagnosticId)
            .hasDraft(false)
            .draftItems(List.of())
            .build();
    }

    /**
     * 보완요청 초안 항목 DTO
     * AI clarifications를 프론트에서 사용하기 쉬운 형태로 변환
     */
    @Builder
    public record RevisionDraftItemDto(
        String slotName,
        String message,
        List<String> fileIds
    ) {
        public static RevisionDraftItemDto from(Clarification clarification) {
            return RevisionDraftItemDto.builder()
                .slotName(clarification.slotName())
                .message(clarification.message())
                .fileIds(clarification.fileIds() != null ? clarification.fileIds() : List.of())
                .build();
        }
    }
}
