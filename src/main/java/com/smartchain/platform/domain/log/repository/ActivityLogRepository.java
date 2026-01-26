package com.smartchain.platform.domain.log.repository;

import com.smartchain.platform.domain.log.entity.ActivityLog;
import com.smartchain.platform.global.enums.ActionType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

@Repository
public interface ActivityLogRepository extends JpaRepository<ActivityLog, Long> {

    Page<ActivityLog> findAllByOrderByCreatedAtDesc(Pageable pageable);

    @Query("SELECT al FROM ActivityLog al WHERE " +
            "(:userId IS NULL OR al.user.userId = :userId) AND " +
            "(:action IS NULL OR al.action = :action) AND " +
            "(:fromDate IS NULL OR al.createdAt >= :fromDate) AND " +
            "(:toDate IS NULL OR al.createdAt <= :toDate) " +
            "ORDER BY al.createdAt DESC")
    Page<ActivityLog> findByFilters(
            @Param("userId") Long userId,
            @Param("action") ActionType action,
            @Param("fromDate") LocalDateTime fromDate,
            @Param("toDate") LocalDateTime toDate,
            Pageable pageable);
}
