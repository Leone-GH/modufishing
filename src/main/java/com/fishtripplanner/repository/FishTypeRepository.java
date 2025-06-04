package com.fishtripplanner.repository;

import com.fishtripplanner.entity.FishTypeEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FishTypeRepository extends JpaRepository<FishTypeEntity, Long> {
}