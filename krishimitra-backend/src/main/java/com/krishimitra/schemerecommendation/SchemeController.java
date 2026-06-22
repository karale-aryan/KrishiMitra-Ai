package com.krishimitra.schemerecommendation;

import com.krishimitra.shared.dto.ApiResponse;
import com.krishimitra.schemerecommendation.dto.SchemeRecommendationResponse;
import com.krishimitra.schemerecommendation.dto.SchemeResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/v1/schemes")
@RequiredArgsConstructor
public class SchemeController {

    private final SchemeRecommendationService schemeRecommendationService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<SchemeResponse>>> getAllActiveSchemes() {
        log.info("Request received to list all active schemes");
        List<SchemeResponse> schemes = schemeRecommendationService.getAllActiveSchemes();
        return ResponseEntity.ok(ApiResponse.success(schemes));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<SchemeResponse>> getSchemeById(@PathVariable UUID id) {
        log.info("Request received for scheme details: {}", id);
        SchemeResponse scheme = schemeRecommendationService.getSchemeById(id);
        return ResponseEntity.ok(ApiResponse.success(scheme));
    }

    @GetMapping("/recommended/{farmerId}")
    public ResponseEntity<ApiResponse<List<SchemeRecommendationResponse>>> getRecommendedSchemes(
            @PathVariable UUID farmerId) {

        log.info("Request received for personalized scheme recommendations for farmer: {}", farmerId);
        List<SchemeRecommendationResponse> recommendations = 
                schemeRecommendationService.getRecommendedSchemes(farmerId);
        return ResponseEntity.ok(ApiResponse.success(recommendations));
    }

    @PatchMapping("/recommendations/{id}/status")
    public ResponseEntity<ApiResponse<SchemeRecommendationResponse>> updateRecommendationStatus(
            @PathVariable UUID id,
            @RequestBody Map<String, String> statusBody) {

        String status = statusBody.get("status");
        log.info("Request received to update recommendation {} status to {}", id, status);
        SchemeRecommendationResponse response = 
                schemeRecommendationService.updateRecommendationStatus(id, status);
        return ResponseEntity.ok(ApiResponse.success("Recommendation status updated", response));
    }
}
