package com.krishimitra.analytics.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Dashboard analytics response with platform-wide aggregate statistics.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardResponse {

    private long totalFarmers;
    private long totalFarms;
    private long totalRecommendations;
    private long totalDiseaseReports;
    private long totalSchemeApplications;
    private long totalVoiceInteractions;
    private long activeUsers7Days;
    private long activeUsers30Days;
}
