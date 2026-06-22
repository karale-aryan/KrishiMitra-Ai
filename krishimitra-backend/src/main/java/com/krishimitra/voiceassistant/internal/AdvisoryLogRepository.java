package com.krishimitra.voiceassistant.internal;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

/**
 * Repository for advisory log persistence and analytics queries.
 */
@Repository
public interface AdvisoryLogRepository extends JpaRepository<AdvisoryLogEntity, UUID> {

    List<AdvisoryLogEntity> findByFarmerIdOrderByCreatedAtDesc(UUID farmerId);

    long countByAdvisoryType(String advisoryType);
}
