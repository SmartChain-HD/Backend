package com.smartchain.platform.domain.user.service;

import com.smartchain.platform.domain.user.entity.Domain;
import com.smartchain.platform.domain.user.repository.DomainRepository;
import com.smartchain.platform.global.error.CustomException;
import com.smartchain.platform.global.error.ErrorCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

/**
 * DomainService 단위 테스트
 */
@ExtendWith(MockitoExtension.class)
class DomainServiceTest {

    @Mock
    private DomainRepository domainRepository;

    @InjectMocks
    private DomainService domainService;

    private Domain esgDomain;
    private Domain safetyDomain;
    private Domain complianceDomain;

    @BeforeEach
    void setUp() {
        esgDomain = Domain.builder()
            .code("ESG")
            .name("ESG 실사")
            .description("ESG 증빙 자동 파싱 및 AI 리포트 생성")
            .isActive(true)
            .build();

        safetyDomain = Domain.builder()
            .code("SAFETY")
            .name("안전보건")
            .description("AI 기반 현장 안전점검(TBM) 자동 검증")
            .isActive(true)
            .build();

        complianceDomain = Domain.builder()
            .code("COMPLIANCE")
            .name("컴플라이언스")
            .description("LLM 기반 하도급 계약서 자동 검토")
            .isActive(false)
            .build();
    }

    @Test
    @DisplayName("활성화된 도메인 목록 조회 성공")
    void getActiveDomains_returnsOnlyActiveDomains() {
        // given
        List<Domain> activeDomains = Arrays.asList(esgDomain, safetyDomain);
        when(domainRepository.findByIsActiveTrue()).thenReturn(activeDomains);

        // when
        List<Domain> result = domainService.getActiveDomains();

        // then
        assertThat(result).hasSize(2);
        assertThat(result).extracting(Domain::getCode)
            .containsExactlyInAnyOrder("ESG", "SAFETY");
    }

    @Test
    @DisplayName("전체 도메인 목록 조회 성공")
    void getAllDomains_returnsAllDomains() {
        // given
        List<Domain> allDomains = Arrays.asList(esgDomain, safetyDomain, complianceDomain);
        when(domainRepository.findAll()).thenReturn(allDomains);

        // when
        List<Domain> result = domainService.getAllDomains();

        // then
        assertThat(result).hasSize(3);
        assertThat(result).extracting(Domain::getCode)
            .containsExactlyInAnyOrder("ESG", "SAFETY", "COMPLIANCE");
    }

    @Test
    @DisplayName("도메인 코드로 조회 성공")
    void getDomainByCode_withValidCode_returnsDomain() {
        // given
        when(domainRepository.findByCode("ESG")).thenReturn(Optional.of(esgDomain));

        // when
        Domain result = domainService.getDomainByCode("ESG");

        // then
        assertThat(result.getCode()).isEqualTo("ESG");
        assertThat(result.getName()).isEqualTo("ESG 실사");
        assertThat(result.getIsActive()).isTrue();
    }

    @Test
    @DisplayName("도메인 코드로 조회 - 소문자 입력도 동작")
    void getDomainByCode_withLowerCaseCode_returnsDomain() {
        // given
        when(domainRepository.findByCode("SAFETY")).thenReturn(Optional.of(safetyDomain));

        // when
        Domain result = domainService.getDomainByCode("safety");

        // then
        assertThat(result.getCode()).isEqualTo("SAFETY");
    }

    @Test
    @DisplayName("존재하지 않는 도메인 코드로 조회 시 예외 발생")
    void getDomainByCode_withInvalidCode_throwsException() {
        // given
        when(domainRepository.findByCode("INVALID")).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> domainService.getDomainByCode("INVALID"))
            .isInstanceOf(CustomException.class)
            .satisfies(ex -> {
                CustomException customEx = (CustomException) ex;
                assertThat(customEx.getErrorCode()).isEqualTo(ErrorCode.DOMAIN_NOT_FOUND);
            });
    }

    @Test
    @DisplayName("도메인 ID로 조회 성공")
    void getDomainById_withValidId_returnsDomain() {
        // given
        when(domainRepository.findById(1L)).thenReturn(Optional.of(esgDomain));

        // when
        Domain result = domainService.getDomainById(1L);

        // then
        assertThat(result.getCode()).isEqualTo("ESG");
    }

    @Test
    @DisplayName("존재하지 않는 도메인 ID로 조회 시 예외 발생")
    void getDomainById_withInvalidId_throwsException() {
        // given
        when(domainRepository.findById(999L)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> domainService.getDomainById(999L))
            .isInstanceOf(CustomException.class)
            .satisfies(ex -> {
                CustomException customEx = (CustomException) ex;
                assertThat(customEx.getErrorCode()).isEqualTo(ErrorCode.DOMAIN_NOT_FOUND);
            });
    }
}
