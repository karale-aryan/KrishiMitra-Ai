package com.krishimitra.schemerecommendation.internal;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * Repository for government scheme queries.
 */
@Repository
public interface SchemeRepository extends JpaRepository<GovernmentSchemeEntity, UUID> {

    List<GovernmentSchemeEntity> findByIsActiveTrueAndValidUntilAfter(LocalDate date);
}
