package com.smartchain.platform.dto.domain;

import com.smartchain.platform.domain.user.entity.Domain;

/**
 * 도메인 응답 DTO (목록/상세 공통)
 */
public record DomainResponse(
    Long domainId,
    String code,
    String name,
    String description,
    Boolean isActive
) {
    /**
     * Domain 엔티티를 응답 DTO로 변환
     */
    public static DomainResponse from(Domain domain) {
        return new DomainResponse(
            domain.getDomainId(),
            domain.getCode(),
            domain.getName(),
            domain.getDescription(),
            domain.getIsActive()
        );
    }
}
