package com.krishimitra.schemerecommendation.internal;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

/**
 * Repository for scheme recommendation persistence and lookup.
 */
@Repository
public interface SchemeRecommendationRepository extends JpaRepository<SchemeRecommendationEntity, UUID> {

    List<SchemeRecommendationEntity> findByFarmerIdOrderByMatchScoreDesc(UUID farmerId);
}
