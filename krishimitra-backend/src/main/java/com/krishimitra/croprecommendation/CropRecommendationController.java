package com.krishimitra.croprecommendation;

import com.krishimitra.croprecommendation.dto.CropRecommendationResponse;
import com.krishimitra.shared.dto.ApiResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * REST controller for crop recommendation endpoints.
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/recommendations")
@RequiredArgsConstructor
public class CropRecommendationController {

    private final CropRecommendationService recommendationService;

    /**
     * Generate crop recommendations for a farm using ML model.
     *
     * @param request map containing farmId
     * @return list of top-5 crop recommendations with confidence scores
     */
    @PostMapping("/generate")
    public ResponseEntity<ApiResponse<List<CropRecommendationResponse>>> generateRecommendations(
            @Valid @RequestBody @NotNull Map<String, UUID> request) {

        UUID farmId = request.get("farmId");
        if (farmId == null) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("farmId is required in request body"));
        }

        log.info("Generating crop recommendations for farm {}", farmId);
        List<CropRecommendationResponse> recommendations = recommendationService.generateRecommendations(farmId);

        return ResponseEntity.ok(ApiResponse.success("Crop recommendations generated successfully", recommendations));
    }

    /**
     * Get past crop recommendations for a farm.
     *
     * @param farmId the UUID of the farm
     * @return list of all recommendations sorted by confidence
     */
    @GetMapping("/{farmId}")
    public ResponseEntity<ApiResponse<List<CropRecommendationResponse>>> getRecommendations(
            @PathVariable UUID farmId) {

        log.info("Fetching crop recommendations for farm {}", farmId);
        List<CropRecommendationResponse> recommendations = recommendationService.getRecommendationsForFarm(farmId);

        return ResponseEntity.ok(ApiResponse.success(recommendations));
    }

    /**
     * Accept (mark as selected) a specific crop recommendation.
     *
     * @param id the UUID of the recommendation
     * @return the updated recommendation
     */
    @PatchMapping("/{id}/accept")
    public ResponseEntity<ApiResponse<CropRecommendationResponse>> acceptRecommendation(
            @PathVariable UUID id) {

        log.info("Accepting crop recommendation {}", id);
        CropRecommendationResponse response = recommendationService.acceptRecommendation(id);

        return ResponseEntity.ok(ApiResponse.success("Recommendation accepted", response));
    }
}
