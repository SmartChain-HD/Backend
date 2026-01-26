package com.smartchain.platform.domain.notification.repository;

import com.smartchain.platform.domain.notification.entity.Notification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    Page<Notification> findByUserUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);

    Page<Notification> findByUserUserIdAndIsReadOrderByCreatedAtDesc(Long userId, boolean isRead, Pageable pageable);

    int countByUserUserIdAndIsRead(Long userId, boolean isRead);

    @Modifying
    @Query("UPDATE Notification n SET n.isRead = true WHERE n.user.userId = :userId AND n.isRead = false")
    int markAllAsReadByUserId(@Param("userId") Long userId);

    @Modifying
    @Query("UPDATE Notification n SET n.isRead = true WHERE n.notificationId IN :ids AND n.user.userId = :userId")
    int markAsReadByIds(@Param("ids") List<Long> ids, @Param("userId") Long userId);
}
