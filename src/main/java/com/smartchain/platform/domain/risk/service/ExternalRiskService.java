package com.smartchain.platform.domain.risk.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.smartchain.platform.domain.risk.client.ExternalRiskApiClient;
import com.smartchain.platform.domain.risk.config.ExternalRiskApiConfig;
import com.smartchain.platform.domain.risk.entity.ExternalRiskResult;
import com.smartchain.platform.domain.risk.repository.ExternalRiskResultRepository;
import com.smartchain.platform.domain.user.entity.Company;
import com.smartchain.platform.domain.user.entity.User;
import com.smartchain.platform.domain.user.repository.CompanyRepository;
import com.smartchain.platform.domain.user.repository.UserRepository;
import com.smartchain.platform.dto.risk.ExternalRiskCompanyResponse;
import com.smartchain.platform.dto.risk.ExternalRiskDetectRequest;
import com.smartchain.platform.dto.risk.ExternalRiskDetectResponse;
import com.smartchain.platform.dto.risk.ExternalRiskResultResponse;
import com.smartchain.platform.global.enums.RiskLevel;
import com.smartchain.platform.global.error.CustomException;
import com.smartchain.platform.global.error.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ExternalRiskService {

    private static final Logger log = LoggerFactory.getLogger(ExternalRiskService.class);

    private final ExternalRiskApiClient externalRiskApiClient;
    private final ExternalRiskApiConfig externalRiskApiConfig;
    private final ExternalRiskResultRepository riskResultRepository;
    private final CompanyRepository companyRepository;
    private final UserRepository userRepository;
    private final ObjectMapper objectMapper;

    @Transactional
    public List<ExternalRiskResultResponse> detect(Long userId, List<Long> companyIds) {
        User user = getUser(userId);
        validateReviewerRole(user);

        List<Company> companies = companyIds.stream()
            .map(id -> companyRepository.findById(id)
                .orElseThrow(() -> new CustomException(ErrorCode.RISK_COMPANY_NOT_FOUND)))
            .toList();

        ExternalRiskDetectRequest request = ExternalRiskDetectRequest.of(companies);
        ExternalRiskDetectResponse response = externalRiskApiClient.detect(request);

        List<ExternalRiskResultResponse> results = new ArrayList<>();

        for (ExternalRiskDetectResponse.VendorRiskResult vendorResult : response.results()) {
            Company company = companies.stream()
                .filter(c -> c.getName().equals(vendorResult.vendor()))
                .findFirst()
                .orElse(null);

            if (company == null) {
                log.warn("AI 응답의 vendor '{}'에 매칭되는 회사를 찾을 수 없습니다", vendorResult.vendor());
                continue;
            }

            RiskLevel riskLevel = RiskLevel.fromString(vendorResult.riskLevel());
            if (riskLevel == null) {
                log.warn("유효하지 않은 riskLevel: {}, 기본값 MEDIUM 적용", vendorResult.riskLevel());
                riskLevel = RiskLevel.MEDIUM;
            }

            String evidenceJson = serializeEvidence(vendorResult.evidence());

            ExternalRiskResult entity = ExternalRiskResult.builder()
                .companyId(company.getCompanyId())
                .companyName(company.getName())
                .riskLevel(riskLevel)
                .summary(vendorResult.summary())
                .evidenceJson(evidenceJson)
                .requestedBy(user.getUserId())
                .build();

            ExternalRiskResult saved = riskResultRepository.save(entity);
            results.add(ExternalRiskResultResponse.from(saved));
        }

        log.info("외부 위험 감지 완료 - 요청: {}개 회사, 결과: {}개", companyIds.size(), results.size());
        return results;
    }

    @Transactional(readOnly = true)
    public ExternalRiskResultResponse getLatestResult(Long userId, Long companyId) {
        User user = getUser(userId);
        validateReviewerRole(user);

        ExternalRiskResult result = riskResultRepository.findTopByCompanyIdOrderByDetectedAtDesc(companyId)
            .orElseThrow(() -> new CustomException(ErrorCode.RISK_RESULT_NOT_FOUND));

        return ExternalRiskResultResponse.from(result);
    }

    @Transactional(readOnly = true)
    public Page<ExternalRiskResultResponse> getAllResults(Long userId, Pageable pageable) {
        User user = getUser(userId);
        validateReviewerRole(user);

        return riskResultRepository.findAllByOrderByDetectedAtDesc(pageable)
            .map(ExternalRiskResultResponse::from);
    }

    @Transactional(readOnly = true)
    public List<ExternalRiskCompanyResponse> getRiskTargetCompanies(Long userId) {
        User user = getUser(userId);
        validateReviewerRole(user);

        List<String> targetNames = externalRiskApiConfig.getTargetCompanies();
        if (targetNames == null || targetNames.isEmpty()) {
            return List.of();
        }

        List<Company> companies = companyRepository.findByNameIn(targetNames);
        return companies.stream()
            .map(ExternalRiskCompanyResponse::from)
            .toList();
    }

    private User getUser(Long userId) {
        return userRepository.findById(userId)
            .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
    }

    private void validateReviewerRole(User user) {
        if (!"REVIEWER".equals(user.getRole().getCode())) {
            throw new CustomException(ErrorCode.RISK_PERMISSION_DENIED);
        }
    }

    private String serializeEvidence(List<ExternalRiskDetectResponse.Evidence> evidence) {
        if (evidence == null || evidence.isEmpty()) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(evidence);
        } catch (JsonProcessingException e) {
            log.warn("evidence 직렬화 실패", e);
            return null;
        }
    }
}
