package com.krishimitra.analytics.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * Farmer-specific analytics response with per-farmer aggregate counts.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FarmerAnalyticsResponse {

    private UUID farmerId;
    private long farmCount;
    private long recommendationCount;
    private long diseaseReportCount;
    private long schemeRecommendationCount;
    private long voiceInteractionCount;
}
