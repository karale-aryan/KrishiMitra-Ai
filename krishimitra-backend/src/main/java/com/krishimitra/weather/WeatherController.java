package com.krishimitra.weather;

import com.krishimitra.shared.dto.ApiResponse;
import com.krishimitra.weather.dto.ClimateRiskResponse;
import com.krishimitra.weather.dto.ForecastDay;
import com.krishimitra.weather.dto.WeatherResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * REST controller for weather and climate risk endpoints.
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/weather")
@RequiredArgsConstructor
public class WeatherController {

    private final WeatherService weatherService;
    private final ClimateRiskService climateRiskService;

    /**
     * Get current weather for a location.
     *
     * @param lat latitude
     * @param lon longitude
     * @return current weather data
     */
    @GetMapping("/current")
    public ResponseEntity<ApiResponse<WeatherResponse>> getCurrentWeather(
            @RequestParam double lat,
            @RequestParam double lon) {

        log.info("Fetching current weather for lat={}, lon={}", lat, lon);
        WeatherResponse weather = weatherService.getCurrentWeather(lat, lon);

        return ResponseEntity.ok(ApiResponse.success(weather));
    }

    /**
     * Get weather forecast for a location.
     *
     * @param lat  latitude
     * @param lon  longitude
     * @param days number of forecast days (default 7)
     * @return list of daily forecast data
     */
    @GetMapping("/forecast")
    public ResponseEntity<ApiResponse<List<ForecastDay>>> getForecast(
            @RequestParam double lat,
            @RequestParam double lon,
            @RequestParam(defaultValue = "7") int days) {

        log.info("Fetching {}-day forecast for lat={}, lon={}", days, lat, lon);
        List<ForecastDay> forecast = weatherService.getForecast(lat, lon, days);

        return ResponseEntity.ok(ApiResponse.success(forecast));
    }

    /**
     * Get climate risk analysis for a farm.
     * Requires authentication.
     *
     * @param farmId the UUID of the farm
     * @return climate risk analysis
     */
    @GetMapping("/risk/{farmId}")
    public ResponseEntity<ApiResponse<ClimateRiskResponse>> getClimateRisk(
            @PathVariable UUID farmId) {

        log.info("Analyzing climate risk for farm {}", farmId);
        ClimateRiskResponse risk = climateRiskService.analyzeRisk(farmId);

        return ResponseEntity.ok(ApiResponse.success("Climate risk analysis completed", risk));
    }
}
