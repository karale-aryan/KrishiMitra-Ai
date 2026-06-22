package com.krishimitra.croprecommendation.internal;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface CropRecommendationRepository extends JpaRepository<CropRecommendationEntity, UUID> {

    List<CropRecommendationEntity> findByFarmIdOrderByConfidenceScoreDesc(UUID farmId);
}
