package com.smartchain.platform.dto.ai;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.smartchain.platform.domain.ai.config.SlotConfigProperties;
import com.smartchain.platform.domain.ai.config.SlotConfigProperties.CrossValidationDefinition;
import com.smartchain.platform.domain.ai.entity.AiAnalysisResult;
import com.smartchain.platform.dto.ai.run.Clarification;
import com.smartchain.platform.dto.ai.run.CrossValidation;
import com.smartchain.platform.dto.ai.run.SlotResult;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * AI 분석 결과 상세 응답 DTO
 * resultJson을 파싱하여 슬롯별 결과와 보완요청 메시지를 구조화하여 제공
 * __x__ 교차 검증 슬롯 및 설정 기반 교차 검증 슬롯은 slotResults에서 분리하여 crossValidations로 제공
 */
public record AiAnalysisResultDetailResponse(
    Long id,
    Long diagnosticId,
    String domainCode,
    String packageId,
    String riskLevel,
    String verdict,
    String whySummary,
    List<SlotResult> slotResults,
    List<CrossValidation> crossValidations,
    List<Clarification> clarifications,
    Map<String, Object> extras,
    LocalDateTime analyzedAt
) {
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final String CROSS_VALIDATION_SEPARATOR = "__x__";

    /**
     * AiAnalysisResult 엔티티를 상세 응답 DTO로 변환 (설정 기반 교차 검증 지원)
     */
    public static AiAnalysisResultDetailResponse from(AiAnalysisResult result, SlotConfigProperties slotConfig) {
        Long diagId = result.getDiagnostic() != null
            ? result.getDiagnostic().getDiagnosticId()
            : null;

        ParsedResult parsed = parseResultJson(result.getResultJson(), slotConfig);

        return new AiAnalysisResultDetailResponse(
            result.getId(),
            diagId,
            result.getDomainCode(),
            result.getPackageId(),
            result.getRiskLevel(),
            result.getVerdict(),
            result.getWhySummary(),
            parsed.slotResults(),
            parsed.crossValidations(),
            parsed.clarifications(),
            parsed.extras(),
            result.getAnalyzedAt()
        );
    }

    /**
     * AiAnalysisResult 엔티티를 상세 응답 DTO로 변환 (하위 호환)
     */
    public static AiAnalysisResultDetailResponse from(AiAnalysisResult result) {
        return from(result, null);
    }

    /**
     * 슬롯명이 교차 검증 슬롯인지 판별 (__x__ 또는 설정 기반)
     */
    static boolean isCrossValidationSlot(String slotName, SlotConfigProperties slotConfig) {
        if (slotName == null) return false;
        if (slotName.contains(CROSS_VALIDATION_SEPARATOR)) return true;
        if (slotConfig != null) return slotConfig.isCrossValidation(slotName);
        return false;
    }

    /**
     * resultJson을 파싱하여 구조화된 데이터 추출
     */
    private static ParsedResult parseResultJson(String resultJson, SlotConfigProperties slotConfig) {
        if (resultJson == null || resultJson.isBlank()) {
            return new ParsedResult(
                Collections.emptyList(),
                Collections.emptyList(),
                Collections.emptyList(),
                Collections.emptyMap()
            );
        }

        try {
            Map<String, Object> jsonMap = OBJECT_MAPPER.readValue(
                resultJson,
                new TypeReference<Map<String, Object>>() {}
            );

            List<SlotResult> allSlotResults = parseSlotResults(jsonMap);

            // __x__ 슬롯 및 설정 기반 교차 검증 슬롯 분리
            List<SlotResult> normalSlots = allSlotResults.stream()
                .filter(sr -> !isCrossValidationSlot(sr.slotName(), slotConfig))
                .toList();

            List<CrossValidation> crossValidations = allSlotResults.stream()
                .filter(sr -> isCrossValidationSlot(sr.slotName(), slotConfig))
                .map(sr -> toCrossValidation(sr, slotConfig))
                .toList();

            List<Clarification> clarifications = parseClarifications(jsonMap);
            Map<String, Object> extras = parseExtras(jsonMap);

            return new ParsedResult(normalSlots, crossValidations, clarifications, extras);
        } catch (Exception e) {
            return new ParsedResult(
                Collections.emptyList(),
                Collections.emptyList(),
                Collections.emptyList(),
                Collections.emptyMap()
            );
        }
    }

    /**
     * SlotResult를 CrossValidation으로 변환 (__x__ 및 설정 기반 모두 지원)
     */
    private static CrossValidation toCrossValidation(SlotResult slotResult, SlotConfigProperties slotConfig) {
        String slotName = slotResult.slotName();

        // __x__ 형식 (기존 로직)
        if (slotName != null && slotName.contains(CROSS_VALIDATION_SEPARATOR)) {
            List<String> slots = Arrays.asList(slotName.split(CROSS_VALIDATION_SEPARATOR));

            List<String> displayNames;
            if (slotResult.displayName() != null && slotResult.displayName().contains(" ↔ ")) {
                displayNames = Arrays.asList(slotResult.displayName().split(" ↔ "));
            } else {
                displayNames = slots;
            }

            return new CrossValidation(
                slots,
                displayNames,
                slotResult.verdict(),
                slotResult.reasons(),
                slotResult.extras()
            );
        }

        // 설정 기반 교차 검증
        if (slotConfig != null) {
            CrossValidationDefinition cvDef = slotConfig.getCrossValidationDefinition(slotName);
            if (cvDef != null) {
                return new CrossValidation(
                    cvDef.getSlots(),
                    List.of(cvDef.getDisplayName()),
                    slotResult.verdict(),
                    slotResult.reasons(),
                    slotResult.extras()
                );
            }
        }

        // fallback
        return new CrossValidation(
            List.of(slotName),
            List.of(slotResult.displayName() != null ? slotResult.displayName() : slotName),
            slotResult.verdict(),
            slotResult.reasons(),
            slotResult.extras()
        );
    }

    @SuppressWarnings("unchecked")
    private static List<SlotResult> parseSlotResults(Map<String, Object> jsonMap) {
        Object slotResultsObj = jsonMap.get("slot_results");
        if (slotResultsObj == null) {
            slotResultsObj = jsonMap.get("slotResults");
        }

        if (slotResultsObj instanceof List<?> list) {
            return list.stream()
                .filter(item -> item instanceof Map)
                .map(item -> {
                    Map<String, Object> map = (Map<String, Object>) item;
                    return new SlotResult(
                        getStringValue(map, "slot_name", "slotName"),
                        getStringValue(map, "display_name", "displayName"),
                        (String) map.get("verdict"),
                        getListValue(map, "reasons"),
                        getListValue(map, "file_ids", "fileIds"),
                        getListValue(map, "file_names", "fileNames"),
                        getMapValue(map, "extras")
                    );
                })
                .toList();
        }
        return Collections.emptyList();
    }

    @SuppressWarnings("unchecked")
    private static List<Clarification> parseClarifications(Map<String, Object> jsonMap) {
        Object clarificationsObj = jsonMap.get("clarifications");

        if (clarificationsObj instanceof List<?> list) {
            return list.stream()
                .filter(item -> item instanceof Map)
                .map(item -> {
                    Map<String, Object> map = (Map<String, Object>) item;
                    return new Clarification(
                        getStringValue(map, "slot_name", "slotName"),
                        (String) map.get("message"),
                        getListValue(map, "file_ids", "fileIds")
                    );
                })
                .toList();
        }
        return Collections.emptyList();
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> parseExtras(Map<String, Object> jsonMap) {
        Object extrasObj = jsonMap.get("extras");
        if (extrasObj instanceof Map) {
            return (Map<String, Object>) extrasObj;
        }
        return Collections.emptyMap();
    }

    private static String getStringValue(Map<String, Object> map, String... keys) {
        for (String key : keys) {
            Object value = map.get(key);
            if (value instanceof String) {
                return (String) value;
            }
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    private static List<String> getListValue(Map<String, Object> map, String... keys) {
        for (String key : keys) {
            Object value = map.get(key);
            if (value instanceof List<?> list) {
                return list.stream()
                    .filter(item -> item instanceof String)
                    .map(item -> (String) item)
                    .toList();
            }
        }
        return Collections.emptyList();
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> getMapValue(Map<String, Object> map, String key) {
        Object value = map.get(key);
        if (value instanceof Map<?, ?> rawMap) {
            Map<String, Object> result = new java.util.HashMap<>();
            rawMap.forEach((k, v) -> {
                if (k instanceof String) {
                    result.put((String) k, v);
                }
            });
            return result;
        }
        return Collections.emptyMap();
    }

    /**
     * 파싱 결과 내부 레코드
     */
    private record ParsedResult(
        List<SlotResult> slotResults,
        List<CrossValidation> crossValidations,
        List<Clarification> clarifications,
        Map<String, Object> extras
    ) {}
}
