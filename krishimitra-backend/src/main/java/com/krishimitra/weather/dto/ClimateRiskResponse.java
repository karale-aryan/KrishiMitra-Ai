package com.krishimitra.weather.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClimateRiskResponse {

    private UUID farmId;
    private RiskLevel overallRisk;
    private RiskDetail droughtRisk;
    private RiskDetail floodRisk;
    private RiskDetail heatStressRisk;
    private RiskDetail waterStressRisk;
    private LocalDate analysisDate;
    private int forecastDays;

    public enum RiskLevel {
        LOW,
        MODERATE,
        HIGH,
        CRITICAL
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RiskDetail {
        private RiskLevel level;
        private int score;
        private String explanation;
        private String explanationHi;
    }
}
