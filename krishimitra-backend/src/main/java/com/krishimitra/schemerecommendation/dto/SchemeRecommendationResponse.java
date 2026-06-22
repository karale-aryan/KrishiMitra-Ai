package com.krishimitra.schemerecommendation.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Response DTO for a personalized scheme recommendation.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SchemeRecommendationResponse {

    private UUID id;
    private UUID farmerId;
    private SchemeResponse scheme;
    private Integer matchScore;
    private List<String> matchReasons;
    private String status;
    private LocalDateTime createdAt;
}
