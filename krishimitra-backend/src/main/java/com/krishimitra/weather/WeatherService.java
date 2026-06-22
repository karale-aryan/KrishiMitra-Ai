package com.krishimitra.weather;

import com.fasterxml.jackson.databind.JsonNode;
import com.krishimitra.weather.dto.ForecastDay;
import com.krishimitra.weather.dto.WeatherResponse;
import com.krishimitra.weather.internal.WeatherRecordEntity;
import com.krishimitra.weather.internal.WeatherRecordRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Service for fetching weather data from the Open-Meteo API.
 * Results are cached to reduce external API calls.
 */
@Slf4j
@Service
public class WeatherService {

    private final WebClient weatherClient;
    private final WeatherRecordRepository weatherRecordRepository;

    public WeatherService(
            @Qualifier("weatherClient") WebClient weatherClient,
            WeatherRecordRepository weatherRecordRepository) {
        this.weatherClient = weatherClient;
        this.weatherRecordRepository = weatherRecordRepository;
    }

    /**
     * Get current weather for a location.
     *
     * @param lat latitude
     * @param lon longitude
     * @return current weather data
     */
    @Cacheable(value = "currentWeather", key = "#lat + '_' + #lon")
    public WeatherResponse getCurrentWeather(double lat, double lon) {
        log.info("Fetching current weather for lat={}, lon={}", lat, lon);

        try {
            JsonNode response = weatherClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/forecast")
                            .queryParam("latitude", lat)
                            .queryParam("longitude", lon)
                            .queryParam("current", "temperature_2m,relative_humidity_2m,precipitation,wind_speed_10m")
                            .queryParam("timezone", "Asia/Kolkata")
                            .build())
                    .retrieve()
                    .bodyToMono(JsonNode.class)
                    .block();

            if (response == null || !response.has("current")) {
                log.warn("Empty or invalid response from weather API for lat={}, lon={}", lat, lon);
                return getDefaultWeather();
            }

            JsonNode current = response.get("current");

            double temperature = getDoubleValue(current, "temperature_2m", 25.0);
            double humidity = getDoubleValue(current, "relative_humidity_2m", 70.0);
            double precipitation = getDoubleValue(current, "precipitation", 0.0);
            double windSpeed = getDoubleValue(current, "wind_speed_10m", 5.0);

            String description = describeWeather(temperature, humidity, precipitation);

            // Persist the weather record
            WeatherRecordEntity record = WeatherRecordEntity.builder()
                    .latitude(lat)
                    .longitude(lon)
                    .temperature(temperature)
                    .humidity(humidity)
                    .precipitation(precipitation)
                    .windSpeed(windSpeed)
                    .recordedAt(LocalDateTime.now())
                    .build();
            weatherRecordRepository.save(record);

            return WeatherResponse.builder()
                    .temperature(temperature)
                    .humidity(humidity)
                    .precipitation(precipitation)
                    .windSpeed(windSpeed)
                    .description(description)
                    .timestamp(LocalDateTime.now())
                    .build();

        } catch (Exception e) {
            log.error("Failed to fetch current weather for lat={}, lon={}: {}", lat, lon, e.getMessage(), e);
            return getDefaultWeather();
        }
    }

    /**
     * Get weather forecast for a location.
     *
     * @param lat  latitude
     * @param lon  longitude
     * @param days number of forecast days (1-16)
     * @return list of daily forecast data
     */
    @Cacheable(value = "weatherForecast", key = "#lat + '_' + #lon + '_' + #days")
    public List<ForecastDay> getForecast(double lat, double lon, int days) {
        log.info("Fetching {}-day weather forecast for lat={}, lon={}", days, lat, lon);

        int forecastDays = Math.max(1, Math.min(days, 16));

        try {
            JsonNode response = weatherClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/forecast")
                            .queryParam("latitude", lat)
                            .queryParam("longitude", lon)
                            .queryParam("daily", "temperature_2m_max,temperature_2m_min,precipitation_sum,precipitation_probability_max")
                            .queryParam("timezone", "Asia/Kolkata")
                            .queryParam("forecast_days", forecastDays)
                            .build())
                    .retrieve()
                    .bodyToMono(JsonNode.class)
                    .block();

            if (response == null || !response.has("daily")) {
                log.warn("Empty or invalid forecast response for lat={}, lon={}", lat, lon);
                return getDefaultForecast(forecastDays);
            }

            JsonNode daily = response.get("daily");
            JsonNode dates = daily.get("time");
            JsonNode tempMax = daily.get("temperature_2m_max");
            JsonNode tempMin = daily.get("temperature_2m_min");
            JsonNode precipSum = daily.get("precipitation_sum");
            JsonNode precipProb = daily.get("precipitation_probability_max");

            List<ForecastDay> forecast = new ArrayList<>();
            int count = dates != null ? dates.size() : 0;

            for (int i = 0; i < count; i++) {
                ForecastDay day = ForecastDay.builder()
                        .date(LocalDate.parse(dates.get(i).asText()))
                        .tempMax(getNodeDouble(tempMax, i, 35.0))
                        .tempMin(getNodeDouble(tempMin, i, 22.0))
                        .precipitationSum(getNodeDouble(precipSum, i, 0.0))
                        .precipitationProbability(getNodeDouble(precipProb, i, 10.0))
                        .build();
                forecast.add(day);
            }

            return forecast;

        } catch (Exception e) {
            log.error("Failed to fetch forecast for lat={}, lon={}: {}", lat, lon, e.getMessage(), e);
            return getDefaultForecast(forecastDays);
        }
    }

    private double getDoubleValue(JsonNode node, String field, double defaultValue) {
        if (node != null && node.has(field) && !node.get(field).isNull()) {
            return node.get(field).asDouble(defaultValue);
        }
        return defaultValue;
    }

    private double getNodeDouble(JsonNode arrayNode, int index, double defaultValue) {
        if (arrayNode != null && index < arrayNode.size() && !arrayNode.get(index).isNull()) {
            return arrayNode.get(index).asDouble(defaultValue);
        }
        return defaultValue;
    }

    private String describeWeather(double temp, double humidity, double precipitation) {
        if (precipitation > 10) {
            return "Heavy rain";
        } else if (precipitation > 2) {
            return "Light rain";
        } else if (humidity > 80) {
            return "Humid and cloudy";
        } else if (temp > 40) {
            return "Extreme heat";
        } else if (temp > 30) {
            return "Hot and sunny";
        } else if (temp > 20) {
            return "Warm and pleasant";
        } else if (temp > 10) {
            return "Cool";
        } else {
            return "Cold";
        }
    }

    private WeatherResponse getDefaultWeather() {
        return WeatherResponse.builder()
                .temperature(25.0)
                .humidity(70.0)
                .precipitation(0.0)
                .windSpeed(5.0)
                .description("Data unavailable - using defaults")
                .timestamp(LocalDateTime.now())
                .build();
    }

    private List<ForecastDay> getDefaultForecast(int days) {
        List<ForecastDay> forecast = new ArrayList<>();
        LocalDate today = LocalDate.now();
        for (int i = 0; i < days; i++) {
            forecast.add(ForecastDay.builder()
                    .date(today.plusDays(i))
                    .tempMax(35.0)
                    .tempMin(22.0)
                    .precipitationSum(5.0)
                    .precipitationProbability(20.0)
                    .build());
        }
        return forecast;
    }
}
