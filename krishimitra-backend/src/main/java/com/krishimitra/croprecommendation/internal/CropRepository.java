package com.krishimitra.croprecommendation.internal;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface CropRepository extends JpaRepository<CropEntity, UUID> {

    Optional<CropEntity> findByCropName(String cropName);
}
