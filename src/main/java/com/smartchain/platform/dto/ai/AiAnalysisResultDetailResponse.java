package com.smartchain.platform.dto.ai;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.smartchain.platform.domain.ai.entity.AiAnalysisResult;
import com.smartchain.platform.dto.ai.run.Clarification;
import com.smartchain.platform.dto.ai.run.SlotResult;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * AI 분석 결과 상세 응답 DTO
 * resultJson을 파싱하여 슬롯별 결과와 보완요청 메시지를 구조화하여 제공
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
    List<Clarification> clarifications,
    Map<String, Object> extras,
    LocalDateTime analyzedAt
) {
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    /**
     * AiAnalysisResult 엔티티를 상세 응답 DTO로 변환
     * resultJson을 파싱하여 슬롯 결과와 보완요청을 추출
     */
    public static AiAnalysisResultDetailResponse from(AiAnalysisResult result) {
        Long diagId = result.getDiagnostic() != null
            ? result.getDiagnostic().getDiagnosticId()
            : null;

        ParsedResult parsed = parseResultJson(result.getResultJson());

        return new AiAnalysisResultDetailResponse(
            result.getId(),
            diagId,
            result.getDomainCode(),
            result.getPackageId(),
            result.getRiskLevel(),
            result.getVerdict(),
            result.getWhySummary(),
            parsed.slotResults(),
            parsed.clarifications(),
            parsed.extras(),
            result.getAnalyzedAt()
        );
    }

    /**
     * resultJson을 파싱하여 구조화된 데이터 추출
     */
    private static ParsedResult parseResultJson(String resultJson) {
        if (resultJson == null || resultJson.isBlank()) {
            return new ParsedResult(
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

            List<SlotResult> slotResults = parseSlotResults(jsonMap);
            List<Clarification> clarifications = parseClarifications(jsonMap);
            Map<String, Object> extras = parseExtras(jsonMap);

            return new ParsedResult(slotResults, clarifications, extras);
        } catch (Exception e) {
            return new ParsedResult(
                Collections.emptyList(),
                Collections.emptyList(),
                Collections.emptyMap()
            );
        }
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
    private static Map<String, String> getMapValue(Map<String, Object> map, String key) {
        Object value = map.get(key);
        if (value instanceof Map<?, ?> rawMap) {
            Map<String, String> result = new java.util.HashMap<>();
            rawMap.forEach((k, v) -> {
                if (k instanceof String && v instanceof String) {
                    result.put((String) k, (String) v);
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
        List<Clarification> clarifications,
        Map<String, Object> extras
    ) {}
}
