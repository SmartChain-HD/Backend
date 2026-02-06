package com.smartchain.platform.domain.user.repository;

import com.smartchain.platform.domain.user.entity.Company;
import com.smartchain.platform.global.enums.CompanyType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CompanyRepository extends JpaRepository<Company, Long> {

    List<Company> findAll();

    @Query("SELECT c FROM Company c WHERE " +
            "(:companyType IS NULL OR c.companyType = :companyType) AND " +
            "(:industryCode IS NULL OR c.industry.code = :industryCode) AND " +
            "(:keyword IS NULL OR LOWER(c.name) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
            "ORDER BY c.createdAt DESC")
    Page<Company> findByFilters(
            @Param("companyType") CompanyType companyType,
            @Param("industryCode") String industryCode,
            @Param("keyword") String keyword,
            Pageable pageable);

    Optional<Company> findByBusinessNumber(String businessNumber);

    boolean existsByBusinessNumber(String businessNumber);

    @Query("SELECT COUNT(u) FROM User u WHERE u.company.companyId = :companyId")
    int countUsersByCompanyId(@Param("companyId") Long companyId);

    @Query("SELECT COUNT(d) FROM Diagnostic d WHERE d.company.companyId = :companyId")
    int countDiagnosticsByCompanyId(@Param("companyId") Long companyId);

    List<Company> findByNameIn(List<String> names);
}
