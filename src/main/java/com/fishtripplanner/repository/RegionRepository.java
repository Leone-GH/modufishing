package com.fishtripplanner.repository;

import com.fishtripplanner.entity.RegionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface RegionRepository extends JpaRepository<RegionEntity, Long> {

    @Query("""
    SELECT DISTINCT r
    FROM RegionEntity r
    LEFT JOIN FETCH r.children c
    WHERE r.parent IS NULL
    ORDER BY r.id
    """)
    List<RegionEntity> findAllWithChildrenOnly();
}
