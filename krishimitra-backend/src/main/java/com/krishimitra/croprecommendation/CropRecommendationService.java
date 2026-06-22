package com.krishimitra.croprecommendation;

import com.krishimitra.croprecommendation.dto.CropRecommendationResponse;
import com.krishimitra.croprecommendation.internal.*;
import com.krishimitra.farm.internal.FarmEntity;
import com.krishimitra.farm.internal.FarmRepository;
import com.krishimitra.shared.exception.BadRequestException;
import com.krishimitra.shared.exception.ResourceNotFoundException;
import com.krishimitra.weather.WeatherService;
import com.krishimitra.weather.dto.WeatherResponse;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.Month;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service for generating and managing crop recommendations.
 * Fetches farm soil data, current weather, and runs ONNX model inference to produce
 * top-5 crop suggestions with confidence scores.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CropRecommendationService {

    private final FarmRepository farmRepository;
    private final CropRepository cropRepository;
    private final CropRecommendationRepository recommendationRepository;
    private final CropOnnxModelService onnxModelService;
    private final WeatherService weatherService;
    private final ObjectMapper objectMapper;

    private static final String MODEL_VERSION = "v1.0.0";

    /**
     * Hindi translations for common crop names.
     */
    private static final Map<String, String> CROP_NAME_HI = Map.ofEntries(
            Map.entry("rice", "चावल"),
            Map.entry("maize", "मक्का"),
            Map.entry("chickpea", "चना"),
            Map.entry("kidneybeans", "राजमा"),
            Map.entry("pigeonpeas", "अरहर"),
            Map.entry("mothbeans", "मोठ"),
            Map.entry("mungbean", "मूंग"),
            Map.entry("blackgram", "उड़द"),
            Map.entry("lentil", "मसूर"),
            Map.entry("pomegranate", "अनार"),
            Map.entry("banana", "केला"),
            Map.entry("mango", "आम"),
            Map.entry("grapes", "अंगूर"),
            Map.entry("watermelon", "तरबूज"),
            Map.entry("muskmelon", "खरबूजा"),
            Map.entry("apple", "सेब"),
            Map.entry("orange", "संतरा"),
            Map.entry("papaya", "पपीता"),
            Map.entry("coconut", "नारियल"),
            Map.entry("cotton", "कपास"),
            Map.entry("jute", "जूट"),
            Map.entry("coffee", "कॉफ़ी")
    );

    /**
     * Generate crop recommendations for a farm.
     * Fetches soil data and current weather, runs ML model, persists top-5 results.
     *
     * @param farmId the UUID of the farm
     * @return list of recommendation responses
     */
    @Transactional
    public List<CropRecommendationResponse> generateRecommendations(UUID farmId) {
        FarmEntity farm = farmRepository.findById(farmId)
                .orElseThrow(() -> new ResourceNotFoundException("Farm", "id", farmId));

        validateFarmData(farm);

        // Fetch current weather data for the farm location
        WeatherResponse weather = fetchWeatherSafe(farm);

        // Build feature vector: [N, P, K, temperature, humidity, pH, rainfall]
        float[] features = new float[]{
                safeFloat(farm.getNitrogenKgHa()),
                safeFloat(farm.getPhosphorusKgHa()),
                safeFloat(farm.getPotassiumKgHa()),
                weather != null ? weather.getTemperature().floatValue() : 25.0f,
                weather != null ? weather.getHumidity().floatValue() : 70.0f,
                safeFloat(farm.getSoilPh()),
                weather != null ? weather.getPrecipitation().floatValue() * 30 : 200.0f // monthly estimate
        };

        String inputFeaturesJson = serializeFeatures(features);
        String season = determineSeason();

        log.info("Generating crop recommendations for farm {} with features N={}, P={}, K={}, temp={}, humidity={}, pH={}, rainfall={}",
                farmId, features[0], features[1], features[2], features[3], features[4], features[5], features[6]);

        // Run ONNX model prediction
        Map<String, Float> predictions = onnxModelService.predict(features);

        // Persist recommendations
        List<CropRecommendationEntity> entities = new ArrayList<>();
        for (Map.Entry<String, Float> entry : predictions.entrySet()) {
            String cropName = entry.getKey();
            Float confidence = entry.getValue();

            CropEntity crop = cropRepository.findByCropName(cropName).orElse(null);
            UUID cropId;
            if (crop != null) {
                cropId = crop.getId();
            } else {
                // Create a minimal crop entry if not in master table
                CropEntity newCrop = CropEntity.builder()
                        .cropName(cropName)
                        .cropNameHi(CROP_NAME_HI.getOrDefault(cropName, cropName))
                        .build();
                newCrop = cropRepository.save(newCrop);
                cropId = newCrop.getId();
                crop = newCrop;
            }

            CropRecommendationEntity recommendation = CropRecommendationEntity.builder()
                    .farmId(farmId)
                    .cropId(cropId)
                    .confidenceScore(confidence.doubleValue())
                    .modelVersion(MODEL_VERSION)
                    .season(season)
                    .inputFeatures(inputFeaturesJson)
                    .isAccepted(false)
                    .build();

            entities.add(recommendationRepository.save(recommendation));
        }

        log.info("Generated {} crop recommendations for farm {}", entities.size(), farmId);

        return entities.stream()
                .map(entity -> mapToResponse(entity, farmId))
                .sorted(Comparator.comparingDouble(CropRecommendationResponse::getConfidenceScore).reversed())
                .collect(Collectors.toList());
    }

    /**
     * Get past recommendations for a farm.
     *
     * @param farmId the UUID of the farm
     * @return list of recommendation responses sorted by confidence
     */
    @Transactional(readOnly = true)
    public List<CropRecommendationResponse> getRecommendationsForFarm(UUID farmId) {
        if (!farmRepository.existsById(farmId)) {
            throw new ResourceNotFoundException("Farm", "id", farmId);
        }

        List<CropRecommendationEntity> entities = recommendationRepository
                .findByFarmIdOrderByConfidenceScoreDesc(farmId);

        return entities.stream()
                .map(entity -> mapToResponse(entity, farmId))
                .collect(Collectors.toList());
    }

    /**
     * Accept a crop recommendation.
     *
     * @param id the UUID of the recommendation
     * @return the updated recommendation response
     */
    @Transactional
    public CropRecommendationResponse acceptRecommendation(UUID id) {
        CropRecommendationEntity entity = recommendationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("CropRecommendation", "id", id));

        entity.setAccepted(true);
        entity = recommendationRepository.save(entity);

        log.info("Recommendation {} accepted", id);

        return mapToResponse(entity, entity.getFarmId());
    }

    private void validateFarmData(FarmEntity farm) {
        if (farm.getNitrogenKgHa() == null || farm.getPhosphorusKgHa() == null
                || farm.getPotassiumKgHa() == null || farm.getSoilPh() == null) {
            throw new BadRequestException(
                    "Farm soil data (N, P, K, pH) is required for crop recommendation. " +
                    "Please update farm soil test results first.");
        }
    }

    private WeatherResponse fetchWeatherSafe(FarmEntity farm) {
        if (farm.getLatitude() == null || farm.getLongitude() == null) {
            log.warn("Farm {} has no location data. Using default weather values.", farm.getId());
            return null;
        }
        try {
            return weatherService.getCurrentWeather(farm.getLatitude(), farm.getLongitude());
        } catch (Exception e) {
            log.warn("Failed to fetch weather for farm {}: {}. Using default values.", farm.getId(), e.getMessage());
            return null;
        }
    }

    private CropRecommendationResponse mapToResponse(CropRecommendationEntity entity, UUID farmId) {
        CropEntity crop = entity.getCrop();
        String cropName = "unknown";
        String cropNameHi = "unknown";

        if (crop != null) {
            cropName = crop.getCropName();
            cropNameHi = crop.getCropNameHi() != null ? crop.getCropNameHi() : cropName;
        } else {
            // Fallback: load crop by ID
            Optional<CropEntity> cropOpt = cropRepository.findById(entity.getCropId());
            if (cropOpt.isPresent()) {
                cropName = cropOpt.get().getCropName();
                cropNameHi = cropOpt.get().getCropNameHi() != null ? cropOpt.get().getCropNameHi() : cropName;
            }
        }

        return CropRecommendationResponse.builder()
                .id(entity.getId())
                .farmId(farmId)
                .cropName(cropName)
                .cropNameHi(cropNameHi)
                .confidenceScore(entity.getConfidenceScore())
                .season(entity.getSeason())
                .modelVersion(entity.getModelVersion())
                .isAccepted(entity.isAccepted())
                .createdAt(entity.getCreatedAt())
                .build();
    }

    private String determineSeason() {
        Month month = LocalDateTime.now().getMonth();
        if (month.getValue() >= 6 && month.getValue() <= 10) {
            return "KHARIF";
        } else if (month.getValue() >= 11 || month.getValue() <= 3) {
            return "RABI";
        } else {
            return "ZAID";
        }
    }

    private float safeFloat(Double value) {
        return value != null ? value.floatValue() : 0.0f;
    }

    private String serializeFeatures(float[] features) {
        try {
            Map<String, Float> featureMap = new LinkedHashMap<>();
            featureMap.put("nitrogen", features[0]);
            featureMap.put("phosphorus", features[1]);
            featureMap.put("potassium", features[2]);
            featureMap.put("temperature", features[3]);
            featureMap.put("humidity", features[4]);
            featureMap.put("ph", features[5]);
            featureMap.put("rainfall", features[6]);
            return objectMapper.writeValueAsString(featureMap);
        } catch (JsonProcessingException e) {
            log.warn("Failed to serialize input features: {}", e.getMessage());
            return "{}";
        }
    }
}
