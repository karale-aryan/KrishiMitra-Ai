package com.krishimitra.diseasedetection.dto;

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
public class DiseaseDetectionResponse {

    private UUID id;
    private UUID farmId;
    private String cropName;
    private String detectedDisease;
    private Double confidenceScore;
    private String severity;
    private String recommendedAction;
    private String recommendedActionHi;
    private String imageUrl;
    private LocalDateTime createdAt;
}
