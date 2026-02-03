package com.smartchain.platform.domain.ai.client;

import com.smartchain.platform.domain.ai.config.AiRunApiConfig;
import com.smartchain.platform.dto.ai.run.RunSubmitRequest;
import com.smartchain.platform.global.error.CustomException;
import com.smartchain.platform.global.error.ErrorCode;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;

import java.io.IOException;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("AiRunApiClient 에러 핸들링 테스트")
class AiRunApiClientErrorHandlingTest {

    private MockWebServer mockWebServer;
    private AiRunApiClient aiRunApiClient;

    @BeforeEach
    void setUp() throws IOException {
        mockWebServer = new MockWebServer();
        mockWebServer.start();

        WebClient webClient = WebClient.builder()
            .baseUrl(mockWebServer.url("/").toString())
            .build();

        AiRunApiConfig config = new AiRunApiConfig();
        config.setMaxRetry(1);  // 빠른 테스트를 위해 재시도 최소화

        aiRunApiClient = new AiRunApiClient(webClient, config);
    }

    @AfterEach
    void tearDown() throws IOException {
        mockWebServer.shutdown();
    }

    @Test
    @DisplayName("4xx 응답 시 AI_BAD_REQUEST 예외를 발생시켜야 한다")
    void shouldThrowAiBadRequestFor4xxResponse() {
        // given
        mockWebServer.enqueue(new MockResponse()
            .setResponseCode(400)
            .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .setBody("{\"error\": \"Invalid domain parameter\"}"));

        RunSubmitRequest request = createTestRequest();

        // when & then
        assertThatThrownBy(() -> aiRunApiClient.submitSync(request))
            .isInstanceOf(CustomException.class)
            .satisfies(ex -> {
                CustomException customEx = (CustomException) ex;
                assertThat(customEx.getErrorCode()).isEqualTo(ErrorCode.AI_BAD_REQUEST);
            });
    }

    @Test
    @DisplayName("422 응답 시 AI_BAD_REQUEST 예외를 발생시켜야 한다")
    void shouldThrowAiBadRequestFor422Response() {
        // given
        mockWebServer.enqueue(new MockResponse()
            .setResponseCode(422)
            .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .setBody("{\"error\": \"Missing required field: files\"}"));

        RunSubmitRequest request = createTestRequest();

        // when & then
        assertThatThrownBy(() -> aiRunApiClient.submitSync(request))
            .isInstanceOf(CustomException.class)
            .satisfies(ex -> {
                CustomException customEx = (CustomException) ex;
                assertThat(customEx.getErrorCode()).isEqualTo(ErrorCode.AI_BAD_REQUEST);
            });
    }

    @Test
    @DisplayName("5xx 응답이 재시도 후에도 지속되면 AI_SERVICE_UNAVAILABLE 예외를 발생시켜야 한다")
    void shouldThrowAiServiceUnavailableAfterRetryExhausted() {
        // given - 재시도 횟수 + 1 만큼 5xx 응답 설정
        mockWebServer.enqueue(new MockResponse().setResponseCode(500));
        mockWebServer.enqueue(new MockResponse().setResponseCode(500));

        RunSubmitRequest request = createTestRequest();

        // when & then
        assertThatThrownBy(() -> aiRunApiClient.submitSync(request))
            .isInstanceOf(CustomException.class)
            .satisfies(ex -> {
                CustomException customEx = (CustomException) ex;
                assertThat(customEx.getErrorCode()).isEqualTo(ErrorCode.AI_SERVICE_UNAVAILABLE);
            });
    }

    @Test
    @DisplayName("성공 응답 시 결과를 정상 반환해야 한다")
    void shouldReturnResponseOnSuccess() {
        // given
        String successResponse = """
            {
                "package_id": "PKG_001",
                "verdict": "PASS",
                "risk_level": "LOW",
                "why": "모든 항목 검증 완료",
                "slot_results": [],
                "clarifications": [],
                "extras": {}
            }
            """;
        mockWebServer.enqueue(new MockResponse()
            .setResponseCode(200)
            .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .setBody(successResponse));

        RunSubmitRequest request = createTestRequest();

        // when
        var response = aiRunApiClient.submitSync(request);

        // then
        assertThat(response).isNotNull();
        assertThat(response.packageId()).isEqualTo("PKG_001");
        assertThat(response.verdict()).isEqualTo("PASS");
        assertThat(response.riskLevel()).isEqualTo("LOW");
    }

    @Test
    @DisplayName("5xx 응답 후 재시도 성공 시 결과를 반환해야 한다")
    void shouldReturnResponseAfterRetrySuccess() {
        // given - 첫 번째는 실패, 두 번째는 성공
        mockWebServer.enqueue(new MockResponse().setResponseCode(500));

        String successResponse = """
            {
                "package_id": "PKG_002",
                "verdict": "WARN",
                "risk_level": "MEDIUM",
                "why": "일부 항목 확인 필요",
                "slot_results": [],
                "clarifications": [],
                "extras": {}
            }
            """;
        mockWebServer.enqueue(new MockResponse()
            .setResponseCode(200)
            .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .setBody(successResponse));

        RunSubmitRequest request = createTestRequest();

        // when
        var response = aiRunApiClient.submitSync(request);

        // then
        assertThat(response).isNotNull();
        assertThat(response.packageId()).isEqualTo("PKG_002");
        assertThat(response.verdict()).isEqualTo("WARN");
    }

    private RunSubmitRequest createTestRequest() {
        return new RunSubmitRequest(
            "PKG_TEST",
            "esg",
            "2024-01-01",
            "2024-12-31",
            List.of(),
            List.of()
        );
    }
}
