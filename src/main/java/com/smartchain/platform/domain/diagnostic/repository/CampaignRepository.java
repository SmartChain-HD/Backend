package com.smartchain.platform.domain.diagnostic.repository;

import com.smartchain.platform.domain.diagnostic.entity.Campaign;
import com.smartchain.platform.domain.user.entity.Domain;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface CampaignRepository extends JpaRepository<Campaign, Long> {
    Optional<Campaign> findByCampaignCode(String campaignCode);

    List<Campaign> findByDomain(Domain domain);

    Page<Campaign> findByDomainOrderByCreatedAtDesc(Domain domain, Pageable pageable);

    List<Campaign> findByIsActiveTrue();

    /**
     * 사용자 권한 도메인에 해당하는 활성 캠페인 목록 조회
     */
    @Query("SELECT c FROM Campaign c WHERE c.domain IN :domains AND c.isActive = true ORDER BY c.createdAt DESC")
    List<Campaign> findByDomainsAndActive(@Param("domains") List<Domain> domains);

    /**
     * 모든 도메인의 활성 캠페인 목록 조회 (REVIEWER용)
     */
    @Query("SELECT c FROM Campaign c WHERE c.isActive = true ORDER BY c.createdAt DESC")
    List<Campaign> findAllActive();
}
