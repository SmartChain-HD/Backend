package com.smartchain.platform.dto.ai;

import com.smartchain.platform.domain.ai.entity.AiAnalysisResult;
import com.smartchain.platform.domain.diagnostic.entity.Diagnostic;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * AI 분석 결과 상세 응답 DTO 테스트
 */
class AiAnalysisResultDetailResponseTest {

    @Test
    @DisplayName("resultJson 파싱 - slot_results와 clarifications 추출 성공")
    void from_withValidResultJson_parsesSlotResultsAndClarifications() {
        // given
        String resultJson = """
            {
                "package_id": "PKG_1_202601_1",
                "risk_level": "MEDIUM",
                "verdict": "PASS",
                "why": "에너지 사용량 증빙 확인 완료",
                "slot_results": [
                    {
                        "slot_name": "esg.energy.usage",
                        "verdict": "PASS",
                        "reasons": ["데이터 형식 적합", "기간 일치"],
                        "file_ids": ["1", "2"],
                        "file_names": ["energy_2026.xlsx", "usage_report.pdf"]
                    },
                    {
                        "slot_name": "esg.hazmat.msds",
                        "verdict": "NEED_FIX",
                        "reasons": ["MSDS 문서 누락"],
                        "file_ids": [],
                        "file_names": []
                    }
                ],
                "clarifications": [
                    {
                        "slot_name": "esg.hazmat.msds",
                        "message": "화학물질 MSDS 문서를 업로드해주세요",
                        "file_ids": []
                    }
                ],
                "extras": {
                    "totalScore": 85,
                    "category": "환경"
                }
            }
            """;

        Diagnostic diagnostic = mock(Diagnostic.class);
        when(diagnostic.getDiagnosticId()).thenReturn(100L);

        AiAnalysisResult result = AiAnalysisResult.builder()
            .diagnostic(diagnostic)
            .domainCode("ESG")
            .packageId("PKG_1_202601_1")
            .riskLevel("MEDIUM")
            .verdict("PASS")
            .whySummary("에너지 사용량 증빙 확인 완료")
            .resultJson(resultJson)
            .analyzedAt(LocalDateTime.now())
            .build();

        // Reflection으로 ID 설정
        try {
            var idField = AiAnalysisResult.class.getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(result, 1L);
        } catch (Exception e) {
            // 테스트 환경에서 ID 설정 실패 시 무시
        }

        // when
        AiAnalysisResultDetailResponse response = AiAnalysisResultDetailResponse.from(result);

        // then
        assertThat(response.diagnosticId()).isEqualTo(100L);
        assertThat(response.domainCode()).isEqualTo("ESG");
        assertThat(response.verdict()).isEqualTo("PASS");
        assertThat(response.riskLevel()).isEqualTo("MEDIUM");

        // slot_results 검증 (일반 슬롯만)
        assertThat(response.slotResults()).hasSize(2);
        assertThat(response.crossValidations()).isEmpty();
        assertThat(response.slotResults().get(0).slotName()).isEqualTo("esg.energy.usage");
        assertThat(response.slotResults().get(0).verdict()).isEqualTo("PASS");
        assertThat(response.slotResults().get(0).reasons()).containsExactly("데이터 형식 적합", "기간 일치");
        assertThat(response.slotResults().get(0).fileNames()).containsExactly("energy_2026.xlsx", "usage_report.pdf");

        assertThat(response.slotResults().get(1).slotName()).isEqualTo("esg.hazmat.msds");
        assertThat(response.slotResults().get(1).verdict()).isEqualTo("NEED_FIX");

        // clarifications 검증
        assertThat(response.clarifications()).hasSize(1);
        assertThat(response.clarifications().get(0).slotName()).isEqualTo("esg.hazmat.msds");
        assertThat(response.clarifications().get(0).message()).isEqualTo("화학물질 MSDS 문서를 업로드해주세요");

        // extras 검증
        assertThat(response.extras()).containsEntry("totalScore", 85);
        assertThat(response.extras()).containsEntry("category", "환경");
    }

    @Test
    @DisplayName("resultJson이 null인 경우 빈 리스트 반환")
    void from_withNullResultJson_returnsEmptyLists() {
        // given
        Diagnostic diagnostic = mock(Diagnostic.class);
        when(diagnostic.getDiagnosticId()).thenReturn(100L);

        AiAnalysisResult result = AiAnalysisResult.builder()
            .diagnostic(diagnostic)
            .domainCode("ESG")
            .packageId("PKG_1_202601_1")
            .riskLevel("LOW")
            .verdict("PASS")
            .whySummary("테스트")
            .resultJson(null)
            .analyzedAt(LocalDateTime.now())
            .build();

        // when
        AiAnalysisResultDetailResponse response = AiAnalysisResultDetailResponse.from(result);

        // then
        assertThat(response.slotResults()).isEmpty();
        assertThat(response.crossValidations()).isEmpty();
        assertThat(response.clarifications()).isEmpty();
        assertThat(response.extras()).isEmpty();
    }

    @Test
    @DisplayName("resultJson이 빈 문자열인 경우 빈 리스트 반환")
    void from_withEmptyResultJson_returnsEmptyLists() {
        // given
        Diagnostic diagnostic = mock(Diagnostic.class);
        when(diagnostic.getDiagnosticId()).thenReturn(100L);

        AiAnalysisResult result = AiAnalysisResult.builder()
            .diagnostic(diagnostic)
            .domainCode("SAFETY")
            .packageId("PKG_2_202601_1")
            .riskLevel("HIGH")
            .verdict("WARN")
            .whySummary("테스트")
            .resultJson("")
            .analyzedAt(LocalDateTime.now())
            .build();

        // when
        AiAnalysisResultDetailResponse response = AiAnalysisResultDetailResponse.from(result);

        // then
        assertThat(response.slotResults()).isEmpty();
        assertThat(response.crossValidations()).isEmpty();
        assertThat(response.clarifications()).isEmpty();
        assertThat(response.extras()).isEmpty();
    }

    @Test
    @DisplayName("잘못된 JSON 형식인 경우 빈 리스트 반환")
    void from_withInvalidJson_returnsEmptyLists() {
        // given
        Diagnostic diagnostic = mock(Diagnostic.class);
        when(diagnostic.getDiagnosticId()).thenReturn(100L);

        AiAnalysisResult result = AiAnalysisResult.builder()
            .diagnostic(diagnostic)
            .domainCode("COMPLIANCE")
            .packageId("PKG_3_202601_1")
            .riskLevel("MEDIUM")
            .verdict("PASS")
            .whySummary("테스트")
            .resultJson("{ invalid json }")
            .analyzedAt(LocalDateTime.now())
            .build();

        // when
        AiAnalysisResultDetailResponse response = AiAnalysisResultDetailResponse.from(result);

        // then
        assertThat(response.slotResults()).isEmpty();
        assertThat(response.crossValidations()).isEmpty();
        assertThat(response.clarifications()).isEmpty();
        assertThat(response.extras()).isEmpty();
        // 기본 필드는 정상적으로 설정
        assertThat(response.domainCode()).isEqualTo("COMPLIANCE");
        assertThat(response.verdict()).isEqualTo("PASS");
    }

    @Test
    @DisplayName("camelCase 키 형식도 파싱 성공")
    void from_withCamelCaseKeys_parsesSuccessfully() {
        // given
        String resultJson = """
            {
                "packageId": "PKG_1_202601_1",
                "riskLevel": "LOW",
                "verdict": "PASS",
                "why": "테스트",
                "slotResults": [
                    {
                        "slotName": "esg.energy.usage",
                        "verdict": "PASS",
                        "reasons": ["OK"],
                        "fileIds": ["1"],
                        "fileNames": ["test.xlsx"]
                    }
                ],
                "clarifications": [],
                "extras": {}
            }
            """;

        Diagnostic diagnostic = mock(Diagnostic.class);
        when(diagnostic.getDiagnosticId()).thenReturn(100L);

        AiAnalysisResult result = AiAnalysisResult.builder()
            .diagnostic(diagnostic)
            .domainCode("ESG")
            .packageId("PKG_1_202601_1")
            .riskLevel("LOW")
            .verdict("PASS")
            .whySummary("테스트")
            .resultJson(resultJson)
            .analyzedAt(LocalDateTime.now())
            .build();

        // when
        AiAnalysisResultDetailResponse response = AiAnalysisResultDetailResponse.from(result);

        // then
        assertThat(response.slotResults()).hasSize(1);
        assertThat(response.slotResults().get(0).slotName()).isEqualTo("esg.energy.usage");
        assertThat(response.slotResults().get(0).fileNames()).containsExactly("test.xlsx");
    }

    @Test
    @DisplayName("__x__ 교차 검증 슬롯이 slotResults에서 분리되어 crossValidations로 이동")
    void from_withCrossValidationSlots_separatesIntoCrossValidations() {
        // given
        String resultJson = """
            {
                "package_id": "PKG_1_202601_1",
                "risk_level": "LOW",
                "verdict": "PASS",
                "why": "안전 교육 증빙 확인 완료",
                "slot_results": [
                    {
                        "slot_name": "safety.education.attendance",
                        "display_name": "교육 출석부",
                        "verdict": "PASS",
                        "reasons": ["출석부 확인 완료"],
                        "file_ids": ["1"],
                        "file_names": ["attendance.xlsx"]
                    },
                    {
                        "slot_name": "safety.education.photo",
                        "display_name": "교육일 사진",
                        "verdict": "PASS",
                        "reasons": ["교육 사진 확인 완료"],
                        "file_ids": ["2"],
                        "file_names": ["photo.jpg"]
                    },
                    {
                        "slot_name": "safety.education.attendance__x__safety.education.photo",
                        "display_name": "교육 출석부 ↔ 교육일 사진",
                        "verdict": "NEED_FIX",
                        "reasons": ["출석 인원 9명, 사진 속 인원 10명 - 불일치"],
                        "file_ids": [],
                        "file_names": [],
                        "extras": {"attendance_count": "9", "photo_count": "10"}
                    }
                ],
                "clarifications": [],
                "extras": {}
            }
            """;

        Diagnostic diagnostic = mock(Diagnostic.class);
        when(diagnostic.getDiagnosticId()).thenReturn(200L);

        AiAnalysisResult result = AiAnalysisResult.builder()
            .diagnostic(diagnostic)
            .domainCode("SAFETY")
            .packageId("PKG_1_202601_1")
            .riskLevel("LOW")
            .verdict("PASS")
            .whySummary("안전 교육 증빙 확인 완료")
            .resultJson(resultJson)
            .analyzedAt(LocalDateTime.now())
            .build();

        // when
        AiAnalysisResultDetailResponse response = AiAnalysisResultDetailResponse.from(result);

        // then - 일반 슬롯만 slotResults에 포함
        assertThat(response.slotResults()).hasSize(2);
        assertThat(response.slotResults().get(0).slotName()).isEqualTo("safety.education.attendance");
        assertThat(response.slotResults().get(1).slotName()).isEqualTo("safety.education.photo");

        // __x__ 슬롯은 crossValidations로 분리
        assertThat(response.crossValidations()).hasSize(1);
        assertThat(response.crossValidations().get(0).slots())
            .containsExactly("safety.education.attendance", "safety.education.photo");
        assertThat(response.crossValidations().get(0).displayNames())
            .containsExactly("교육 출석부", "교육일 사진");
        assertThat(response.crossValidations().get(0).verdict()).isEqualTo("NEED_FIX");
        assertThat(response.crossValidations().get(0).reasons())
            .containsExactly("출석 인원 9명, 사진 속 인원 10명 - 불일치");
        assertThat(response.crossValidations().get(0).extras())
            .containsEntry("attendance_count", "9")
            .containsEntry("photo_count", "10");
    }

    @Test
    @DisplayName("__x__ 슬롯에 displayName이 없으면 슬롯명을 displayNames로 사용")
    void from_withCrossValidationWithoutDisplayName_usesSlotNames() {
        // given
        String resultJson = """
            {
                "package_id": "PKG_1_202601_1",
                "risk_level": "LOW",
                "verdict": "PASS",
                "why": "테스트",
                "slot_results": [
                    {
                        "slot_name": "safety.tbm.checklist__x__safety.tbm.video",
                        "verdict": "PASS",
                        "reasons": ["일치"],
                        "file_ids": [],
                        "file_names": []
                    }
                ],
                "clarifications": [],
                "extras": {}
            }
            """;

        Diagnostic diagnostic = mock(Diagnostic.class);
        when(diagnostic.getDiagnosticId()).thenReturn(300L);

        AiAnalysisResult result = AiAnalysisResult.builder()
            .diagnostic(diagnostic)
            .domainCode("SAFETY")
            .packageId("PKG_1_202601_1")
            .riskLevel("LOW")
            .verdict("PASS")
            .whySummary("테스트")
            .resultJson(resultJson)
            .analyzedAt(LocalDateTime.now())
            .build();

        // when
        AiAnalysisResultDetailResponse response = AiAnalysisResultDetailResponse.from(result);

        // then - slotResults에서 제외
        assertThat(response.slotResults()).isEmpty();

        // crossValidations에 포함, displayName 없으므로 슬롯명 사용
        assertThat(response.crossValidations()).hasSize(1);
        assertThat(response.crossValidations().get(0).slots())
            .containsExactly("safety.tbm.checklist", "safety.tbm.video");
        assertThat(response.crossValidations().get(0).displayNames())
            .containsExactly("safety.tbm.checklist", "safety.tbm.video");
        assertThat(response.crossValidations().get(0).verdict()).isEqualTo("PASS");
    }
}
