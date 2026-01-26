package com.smartchain.platform.domain.user.repository;

import com.smartchain.platform.domain.user.entity.Domain;
import com.smartchain.platform.domain.user.entity.User;
import com.smartchain.platform.domain.user.entity.UserDomainRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserDomainRoleRepository extends JpaRepository<UserDomainRole, Long> {

    List<UserDomainRole> findByUser(User user);

    List<UserDomainRole> findByUserUserId(Long userId);

    Optional<UserDomainRole> findByUserAndDomain(User user, Domain domain);

    Optional<UserDomainRole> findByUserUserIdAndDomainDomainId(Long userId, Long domainId);

    boolean existsByUserAndDomain(User user, Domain domain);

    boolean existsByUserUserIdAndDomainDomainId(Long userId, Long domainId);

    @Query("SELECT udr FROM UserDomainRole udr " +
           "JOIN FETCH udr.domain " +
           "JOIN FETCH udr.role " +
           "WHERE udr.user.userId = :userId")
    List<UserDomainRole> findByUserIdWithDomainAndRole(@Param("userId") Long userId);

    @Query("SELECT udr FROM UserDomainRole udr " +
           "WHERE udr.user.userId = :userId AND udr.domain.code = :domainCode")
    Optional<UserDomainRole> findByUserIdAndDomainCode(@Param("userId") Long userId, @Param("domainCode") String domainCode);

    void deleteByUserAndDomain(User user, Domain domain);
}