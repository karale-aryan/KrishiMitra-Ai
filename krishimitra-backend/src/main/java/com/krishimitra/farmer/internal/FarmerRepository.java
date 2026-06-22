package com.krishimitra.farmer.internal;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/**
 * Repository for {@link FarmerEntity} persistence operations.
 */
@Repository
public interface FarmerRepository extends JpaRepository<FarmerEntity, UUID> {

    Optional<FarmerEntity> findByUserId(UUID userId);

    boolean existsByUserId(UUID userId);

    Page<FarmerEntity> findByState(String state, Pageable pageable);

    Page<FarmerEntity> findByDistrict(String district, Pageable pageable);
}
