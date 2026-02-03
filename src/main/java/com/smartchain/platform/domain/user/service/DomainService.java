package com.smartchain.platform.domain.user.service;

import com.smartchain.platform.domain.user.entity.Domain;
import com.smartchain.platform.domain.user.repository.DomainRepository;
import com.smartchain.platform.global.error.CustomException;
import com.smartchain.platform.global.error.ErrorCode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 도메인 관리 서비스
 */
@Service
@Transactional(readOnly = true)
public class DomainService {

    private final DomainRepository domainRepository;

    public DomainService(DomainRepository domainRepository) {
        this.domainRepository = domainRepository;
    }

    /**
     * 활성화된 도메인 목록 조회
     */
    public List<Domain> getActiveDomains() {
        return domainRepository.findByIsActiveTrue();
    }

    /**
     * 전체 도메인 목록 조회
     */
    public List<Domain> getAllDomains() {
        return domainRepository.findAll();
    }

    /**
     * 도메인 코드로 상세 조회
     */
    public Domain getDomainByCode(String code) {
        return domainRepository.findByCode(code.toUpperCase())
            .orElseThrow(() -> new CustomException(ErrorCode.DOMAIN_NOT_FOUND));
    }

    /**
     * 도메인 ID로 상세 조회
     */
    public Domain getDomainById(Long domainId) {
        return domainRepository.findById(domainId)
            .orElseThrow(() -> new CustomException(ErrorCode.DOMAIN_NOT_FOUND));
    }
}
