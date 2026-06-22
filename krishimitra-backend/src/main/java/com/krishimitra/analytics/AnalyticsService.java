package com.krishimitra.analytics;

import com.krishimitra.analytics.dto.DashboardResponse;
import com.krishimitra.analytics.dto.FarmerAnalyticsResponse;
import com.krishimitra.farmer.internal.FarmerRepository;
import com.krishimitra.farm.internal.FarmRepository;
import com.krishimitra.farm.internal.FarmEntity;
import com.krishimitra.croprecommendation.internal.CropRecommendationRepository;
import com.krishimitra.diseasedetection.internal.DiseaseReportRepository;
import com.krishimitra.diseasedetection.internal.DiseaseReportEntity;
import com.krishimitra.schemerecommendation.internal.SchemeRecommendationRepository;
import com.krishimitra.voiceassistant.internal.AdvisoryLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/**
 * Service that aggregates platform-wide and farmer-specific analytics
 * by querying across all module repositories.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AnalyticsService {

    private final FarmerRepository farmerRepository;
    private final FarmRepository farmRepository;
    private final CropRecommendationRepository cropRecommendationRepository;
    private final DiseaseReportRepository diseaseReportRepository;
    private final SchemeRecommendationRepository schemeRecommendationRepository;
    private final AdvisoryLogRepository advisoryLogRepository;

    /**
     * Aggregates platform-wide statistics for the admin dashboard.
     * Counts are drawn from all available module repositories.
     */
    @Transactional(readOnly = true)
    public DashboardResponse getDashboardStats() {
        log.info("Generating platform dashboard statistics");

        long totalFarmers = farmerRepository.count();
        long totalFarms = farmRepository.count();
        long totalRecommendations = cropRecommendationRepository.count();
        long totalDiseaseReports = diseaseReportRepository.count();
        long totalSchemeApplications = schemeRecommendationRepository.count();
        long totalVoiceInteractions = advisoryLogRepository.count();

        DashboardResponse response = DashboardResponse.builder()
                .totalFarmers(totalFarmers)
                .totalFarms(totalFarms)
                .totalRecommendations(totalRecommendations)
                .totalDiseaseReports(totalDiseaseReports)
                .totalSchemeApplications(totalSchemeApplications)
                .totalVoiceInteractions(totalVoiceInteractions)
                .activeUsers7Days(totalFarmers) // approximation for now
                .activeUsers30Days(totalFarmers)
                .build();

        log.info("Dashboard stats: farmers={}, farms={}, schemes={}, voice={}", 
                totalFarmers, totalFarms, totalSchemeApplications, totalVoiceInteractions);
        return response;
    }

    /**
     * Aggregates analytics for a specific farmer.
     *
     * @param farmerId the farmer's UUID
     * @return farmer-specific activity counts
     */
    @Transactional(readOnly = true)
    public FarmerAnalyticsResponse getFarmerAnalytics(UUID farmerId) {
        log.info("Generating analytics for farmerId: {}", farmerId);

        // Find all farms for this farmer
        List<FarmEntity> farms = farmRepository.findByFarmerId(farmerId);
        long farmCount = farms.size();

        // Recommendations across all farms owned by this farmer
        long recommendationCount = 0;
        long diseaseReportCount = 0;

        for (FarmEntity farm : farms) {
            recommendationCount += cropRecommendationRepository
                    .findByFarmIdOrderByConfidenceScoreDesc(farm.getId()).size();
            diseaseReportCount += diseaseReportRepository
                    .findByFarmIdOrderByCreatedAtDesc(farm.getId()).size();
        }

        long schemeRecommendationCount = schemeRecommendationRepository
                .findByFarmerIdOrderByMatchScoreDesc(farmerId).size();
        long voiceInteractionCount = advisoryLogRepository
                .findByFarmerIdOrderByCreatedAtDesc(farmerId).size();

        FarmerAnalyticsResponse response = FarmerAnalyticsResponse.builder()
                .farmerId(farmerId)
                .farmCount(farmCount)
                .recommendationCount(recommendationCount)
                .diseaseReportCount(diseaseReportCount)
                .schemeRecommendationCount(schemeRecommendationCount)
                .voiceInteractionCount(voiceInteractionCount)
                .build();

        log.info("Farmer {} analytics: farms={}, recommendations={}, diseases={}, schemes={}, voice={}", 
                farmerId, farmCount, recommendationCount, diseaseReportCount, schemeRecommendationCount, voiceInteractionCount);
        return response;
    }
}

