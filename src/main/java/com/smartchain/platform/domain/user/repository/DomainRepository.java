package com.smartchain.platform.domain.user.repository;

import com.smartchain.platform.domain.user.entity.Domain;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DomainRepository extends JpaRepository<Domain, Long> {

    Optional<Domain> findByCode(String code);

    List<Domain> findByIsActiveTrue();

    boolean existsByCode(String code);
}
