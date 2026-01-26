package com.smartchain.platform.domain.role.repository;

import com.smartchain.platform.domain.user.entity.Company;
import com.smartchain.platform.domain.user.entity.Domain;
import com.smartchain.platform.domain.user.entity.RoleRequest;
import com.smartchain.platform.domain.user.entity.User;
import com.smartchain.platform.global.enums.RequestStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RoleRequestRepository extends JpaRepository<RoleRequest, Long> {

    Optional<RoleRequest> findByUserAndStatus(User user, RequestStatus status);

    Optional<RoleRequest> findTopByUserOrderByCreatedAtDesc(User user);

    Optional<RoleRequest> findByRequestId(Long requestId);

    boolean existsByUserAndStatus(User user, RequestStatus status);

    // 도메인별 조회
    boolean existsByUserAndDomainAndStatus(User user, Domain domain, RequestStatus status);

    Optional<RoleRequest> findByUserAndDomainAndStatus(User user, Domain domain, RequestStatus status);

    // 전체 조회 (페이징)
    Page<RoleRequest> findAllByOrderByCreatedAtDesc(Pageable pageable);

    Page<RoleRequest> findByStatusOrderByCreatedAtDesc(RequestStatus status, Pageable pageable);

    // 회사별 조회
    Page<RoleRequest> findByCompanyOrderByCreatedAtDesc(Company company, Pageable pageable);

    Page<RoleRequest> findByCompanyAndStatusOrderByCreatedAtDesc(Company company, RequestStatus status, Pageable pageable);

    // 요청 역할별 조회
    Page<RoleRequest> findByRequestedRoleOrderByCreatedAtDesc(String requestedRole, Pageable pageable);

    Page<RoleRequest> findByRequestedRoleAndStatusOrderByCreatedAtDesc(String requestedRole, RequestStatus status, Pageable pageable);

    // 통계용 카운트
    long countByStatus(RequestStatus status);
}
