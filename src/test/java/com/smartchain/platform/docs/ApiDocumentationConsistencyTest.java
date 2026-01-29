package com.smartchain.platform.docs;

import io.swagger.v3.oas.annotations.Parameter;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * API 문서 일관성 테스트
 * Swagger 어노테이션의 도메인 코드 표기 통일성 검증
 */
@DisplayName("API 문서 일관성 테스트")
class ApiDocumentationConsistencyTest {

    private static final List<String> VALID_DOMAIN_CODES = Arrays.asList("ESG", "SAFETY", "COMPLIANCE");
    private static final List<String> DEPRECATED_DOMAIN_CODES = Arrays.asList("ENV", "SOC", "GOV");

    @Test
    @DisplayName("DiagnosticController의 domainCode 파라미터 Swagger 설명에 올바른 코드가 포함되어야 함")
    void diagnosticController_shouldHaveCorrectDomainCodes() throws Exception {
        Class<?> controllerClass = Class.forName(
            "com.smartchain.platform.domain.diagnostic.controller.DiagnosticController"
        );

        String domainParamDesc = findDomainCodeParameterDescription(controllerClass, "getDiagnosticList");
        assertThat(domainParamDesc)
            .as("domainCode 파라미터 설명이 존재해야 함")
            .isNotNull();

        // 올바른 도메인 코드 포함 확인
        assertThat(domainParamDesc)
            .as("ESG, SAFETY, COMPLIANCE 중 하나가 포함되어야 함")
            .containsAnyOf("ESG", "SAFETY", "COMPLIANCE");

        // 잘못된 도메인 코드 미포함 확인
        for (String deprecated : DEPRECATED_DOMAIN_CODES) {
            assertThat(domainParamDesc)
                .as("deprecated 도메인 코드 '%s'가 포함되면 안 됨", deprecated)
                .doesNotContain(deprecated);
        }
    }

    @Test
    @DisplayName("ApprovalController의 domainCode 파라미터 Swagger 설명에 올바른 코드가 포함되어야 함")
    void approvalController_shouldHaveCorrectDomainCodes() throws Exception {
        Class<?> controllerClass = Class.forName(
            "com.smartchain.platform.domain.approval.controller.ApprovalController"
        );

        String domainParamDesc = findDomainCodeParameterDescription(controllerClass, "getApprovalList");
        assertThat(domainParamDesc).isNotNull();
        assertThat(domainParamDesc).containsAnyOf("ESG", "SAFETY", "COMPLIANCE");
    }

    @Test
    @DisplayName("ReviewController의 domainCode 파라미터 Swagger 설명에 올바른 코드가 포함되어야 함")
    void reviewController_shouldHaveCorrectDomainCodes() throws Exception {
        Class<?> controllerClass = Class.forName(
            "com.smartchain.platform.domain.review.controller.ReviewController"
        );

        String domainParamDesc = findDomainCodeParameterDescription(controllerClass, "getReviewList");
        assertThat(domainParamDesc).isNotNull();
        assertThat(domainParamDesc).containsAnyOf("ESG", "SAFETY", "COMPLIANCE");
    }

    @Test
    @DisplayName("유효한 도메인 코드 목록은 ESG, SAFETY, COMPLIANCE 3개여야 함")
    void validDomainCodes_shouldBeThree() {
        assertThat(VALID_DOMAIN_CODES)
            .hasSize(3)
            .containsExactlyInAnyOrder("ESG", "SAFETY", "COMPLIANCE");
    }

    /**
     * 컨트롤러 클래스의 특정 메서드에서 domainCode 관련 @Parameter 어노테이션의 description을 찾음
     */
    private String findDomainCodeParameterDescription(Class<?> controllerClass, String methodName) {
        Method method = Arrays.stream(controllerClass.getDeclaredMethods())
            .filter(m -> m.getName().equals(methodName))
            .findFirst()
            .orElseThrow(() -> new RuntimeException("Method not found: " + methodName));

        // 메서드의 모든 파라미터에서 @Parameter 어노테이션 추출
        return Arrays.stream(method.getParameterAnnotations())
            .flatMap(Arrays::stream)
            .filter(a -> a.annotationType().equals(Parameter.class))
            .map(a -> ((Parameter) a).description())
            .filter(desc -> desc.toLowerCase().contains("도메인") || desc.toLowerCase().contains("domain"))
            .findFirst()
            .orElse(null);
    }
}
