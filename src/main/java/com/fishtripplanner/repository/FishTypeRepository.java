package com.fishtripplanner.repository;

import com.fishtripplanner.entity.FishTypeEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FishTypeRepository extends JpaRepository<FishTypeEntity, Long> {

    // ✅ 어종 이름 리스트로 어종 조회
    List<FishTypeEntity> findByNameIn(List<String> names);
}
