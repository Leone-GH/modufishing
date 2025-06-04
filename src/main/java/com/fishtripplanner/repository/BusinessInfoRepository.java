package com.fishtripplanner.repository;

import com.fishtripplanner.domain.BusinessInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface BusinessInfoRepository extends JpaRepository<BusinessInfo, Long> {

    @Query("SELECT b.companyName FROM BusinessInfo b WHERE b.user.id = :userId")
    Optional<String> findCompanyNameByUserId(@Param("userId") Long userId);
}