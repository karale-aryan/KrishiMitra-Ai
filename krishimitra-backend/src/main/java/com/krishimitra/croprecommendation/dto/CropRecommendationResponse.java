package com.krishimitra.croprecommendation.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CropRecommendationResponse {

    private UUID id;
    private UUID farmId;
    private String cropName;
    private String cropNameHi;
    private Double confidenceScore;
    private String season;
    private String modelVersion;
    private boolean isAccepted;
    private LocalDateTime createdAt;
}
