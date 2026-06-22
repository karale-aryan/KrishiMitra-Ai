package com.krishimitra.farm.internal;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

/**
 * Repository for {@link FarmEntity} persistence operations.
 */
@Repository
public interface FarmRepository extends JpaRepository<FarmEntity, UUID> {

    List<FarmEntity> findByFarmerId(UUID farmerId);
}
