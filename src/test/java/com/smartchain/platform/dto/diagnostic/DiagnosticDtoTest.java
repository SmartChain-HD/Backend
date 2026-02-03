package com.smartchain.platform.dto.diagnostic;

import com.smartchain.platform.dto.diagnostic.common.DomainSimpleDto;
import com.smartchain.platform.dto.diagnostic.create.DiagnosticCreateRequest;
import com.smartchain.platform.dto.diagnostic.create.DiagnosticCreateResponse;
import com.smartchain.platform.dto.diagnostic.detail.DiagnosticDetailResponse;
import com.smartchain.platform.dto.diagnostic.list.DiagnosticListItemDto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class DiagnosticDtoTest {

    @Test
    @DisplayName("DiagnosticCreateRequest에 domainCode 필드가 포함됨")
    void diagnosticCreateRequest_hasDomainCode() {
        // given
        DiagnosticCreateRequest request = DiagnosticCreateRequest.builder()
                .campaignId(1L)
                .domainCode("ESG")
                .build();

        // then
        assertThat(request.getCampaignId()).isEqualTo(1L);
        assertThat(request.getDomainCode()).isEqualTo("ESG");
    }

    @Test
    @DisplayName("DiagnosticCreateResponse에 domainCode, domainName 필드가 포함됨")
    void diagnosticCreateResponse_hasDomainInfo() {
        // given
        DiagnosticCreateResponse response = DiagnosticCreateResponse.builder()
                .diagnosticId(1L)
                .diagnosticCode("DG-2026-00001")
                .domainCode("ESG")
                .domainName("ESG 실사")
                .status("WRITING")
                .createdAt(LocalDateTime.now())
                .build();

        // then
        assertThat(response.getDiagnosticId()).isEqualTo(1L);
        assertThat(response.getDomainCode()).isEqualTo("ESG");
        assertThat(response.getDomainName()).isEqualTo("ESG 실사");
    }

    @Test
    @DisplayName("DiagnosticDetailResponse에 domain 정보가 포함됨")
    void diagnosticDetailResponse_hasDomainInfo() {
        // given
        DomainSimpleDto domain = DomainSimpleDto.builder()
                .domainId(1L)
                .code("ESG")
                .name("ESG 실사")
                .build();

        DiagnosticDetailResponse response = DiagnosticDetailResponse.builder()
                .diagnosticId(1L)
                .diagnosticCode("DG-2026-00001")
                .domain(domain)
                .status("WRITING")
                .statusLabel("작성중")
                .build();

        // then
        assertThat(response.getDomain()).isNotNull();
        assertThat(response.getDomain().getDomainId()).isEqualTo(1L);
        assertThat(response.getDomain().getCode()).isEqualTo("ESG");
        assertThat(response.getDomain().getName()).isEqualTo("ESG 실사");
    }

    @Test
    @DisplayName("DiagnosticListItemDto에 domain 정보가 포함됨")
    void diagnosticListItemDto_hasDomainInfo() {
        // given
        DomainSimpleDto domain = DomainSimpleDto.builder()
                .domainId(1L)
                .code("ESG")
                .name("ESG 실사")
                .build();

        DiagnosticListItemDto item = DiagnosticListItemDto.builder()
                .diagnosticId(1L)
                .diagnosticCode("DG-2026-00001")
                .domain(domain)
                .status("WRITING")
                .statusLabel("작성중")
                .build();

        // then
        assertThat(item.getDomain()).isNotNull();
        assertThat(item.getDomain().getDomainId()).isEqualTo(1L);
        assertThat(item.getDomain().getCode()).isEqualTo("ESG");
        assertThat(item.getDomain().getName()).isEqualTo("ESG 실사");
    }

    @Test
    @DisplayName("DomainSimpleDto 빌더 및 getter 테스트")
    void domainSimpleDto_builderAndGetterTest() {
        // given
        DomainSimpleDto dto = DomainSimpleDto.builder()
                .domainId(1L)
                .code("SAFETY")
                .name("안전보건")
                .build();

        // then
        assertThat(dto.getDomainId()).isEqualTo(1L);
        assertThat(dto.getCode()).isEqualTo("SAFETY");
        assertThat(dto.getName()).isEqualTo("안전보건");
    }
}
