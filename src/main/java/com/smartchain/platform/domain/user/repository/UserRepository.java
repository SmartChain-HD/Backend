package com.smartchain.platform.domain.user.repository;

import com.smartchain.platform.domain.user.entity.User;
import com.smartchain.platform.global.enums.UserStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    // 사용자 관리 - 전체 조회 (페이징)
    Page<User> findAllByOrderByCreatedAtDesc(Pageable pageable);

    // 상태별 조회
    Page<User> findByStatusOrderByCreatedAtDesc(UserStatus status, Pageable pageable);

    // 역할별 조회
    @Query("SELECT u FROM User u WHERE u.role.code = :roleCode ORDER BY u.createdAt DESC")
    Page<User> findByRoleCodeOrderByCreatedAtDesc(@Param("roleCode") String roleCode, Pageable pageable);

    // 회사별 조회
    @Query("SELECT u FROM User u WHERE u.company.companyId = :companyId ORDER BY u.createdAt DESC")
    Page<User> findByCompanyIdOrderByCreatedAtDesc(@Param("companyId") Long companyId, Pageable pageable);

    // 키워드 검색 (이름 또는 이메일)
    @Query("SELECT u FROM User u WHERE u.name LIKE %:keyword% OR u.email LIKE %:keyword% ORDER BY u.createdAt DESC")
    Page<User> findByKeywordOrderByCreatedAtDesc(@Param("keyword") String keyword, Pageable pageable);

    // 통계용 카운트
    long countByStatus(UserStatus status);

    // 역할별 사용자 목록 조회 (알림 발송용)
    @Query("SELECT u FROM User u WHERE u.role.code = :roleCode")
    java.util.List<User> findAllByRoleCode(@Param("roleCode") String roleCode);

    @Query("SELECT COUNT(u) FROM User u WHERE u.lastLoginAt >= :since")
    long countByLastLoginAtAfter(@Param("since") LocalDateTime since);
}
