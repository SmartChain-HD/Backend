package com.smartchain.platform.domain.ai.service;

import com.smartchain.platform.dto.ai.run.SlotHint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AiAnalysisAsyncService {

    private static final Logger log = LoggerFactory.getLogger(AiAnalysisAsyncService.class);

    private final AiAnalysisService aiAnalysisService;

    public AiAnalysisAsyncService(AiAnalysisService aiAnalysisService) {
        this.aiAnalysisService = aiAnalysisService;
    }

    @Async
    public void submitAsync(Long diagnosticId, List<SlotHint> slotHints) {
        try {
            aiAnalysisService.submit(diagnosticId, slotHints);
        } catch (Exception e) {
            log.error("AI 분석 실패 - diagnosticId: {}", diagnosticId, e);
        }
    }
}
