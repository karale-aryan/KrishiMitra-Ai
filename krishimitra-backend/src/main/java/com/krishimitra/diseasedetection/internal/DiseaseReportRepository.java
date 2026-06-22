package com.krishimitra.diseasedetection.internal;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface DiseaseReportRepository extends JpaRepository<DiseaseReportEntity, UUID> {

    List<DiseaseReportEntity> findByFarmIdOrderByCreatedAtDesc(UUID farmId);
}
