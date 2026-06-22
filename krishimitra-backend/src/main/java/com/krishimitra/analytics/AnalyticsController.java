package com.krishimitra.analytics;

import com.krishimitra.analytics.dto.DashboardResponse;
import com.krishimitra.analytics.dto.FarmerAnalyticsResponse;
import com.krishimitra.shared.dto.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/v1/analytics")
@RequiredArgsConstructor
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    @GetMapping("/dashboard")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<DashboardResponse>> getDashboardStats() {
        log.info("Request received for admin dashboard statistics");
        DashboardResponse stats = analyticsService.getDashboardStats();
        return ResponseEntity.ok(ApiResponse.success(stats));
    }

    @GetMapping("/farmer/{farmerId}")
    public ResponseEntity<ApiResponse<FarmerAnalyticsResponse>> getFarmerAnalytics(@PathVariable UUID farmerId) {
        log.info("Request received for farmer analytics, farmerId: {}", farmerId);
        FarmerAnalyticsResponse stats = analyticsService.getFarmerAnalytics(farmerId);
        return ResponseEntity.ok(ApiResponse.success(stats));
    }
}
