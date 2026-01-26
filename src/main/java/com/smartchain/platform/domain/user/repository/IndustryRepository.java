package com.smartchain.platform.domain.user.repository;

import com.smartchain.platform.domain.user.entity.Industry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface IndustryRepository extends JpaRepository<Industry, Long> {

    Optional<Industry> findByCode(String code);
}