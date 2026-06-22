package com.krishimitra.schemerecommendation;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.krishimitra.schemerecommendation.dto.SchemeRecommendationResponse;
import com.krishimitra.schemerecommendation.dto.SchemeResponse;
import com.krishimitra.schemerecommendation.internal.GovernmentSchemeEntity;
import com.krishimitra.schemerecommendation.internal.SchemeRecommendationEntity;
import com.krishimitra.schemerecommendation.internal.SchemeRecommendationRepository;
import com.krishimitra.schemerecommendation.internal.SchemeRepository;
import com.krishimitra.shared.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service for government scheme management and personalized recommendation engine.
 * Uses a rules-based engine to match farmers with eligible schemes based on
 * state, land holding, and income category criteria.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SchemeRecommendationService {

    private final SchemeRepository schemeRepository;
    private final SchemeRecommendationRepository schemeRecommendationRepository;
    private final ObjectMapper objectMapper;

    /**
     * Returns all active government schemes that have not yet expired.
     */
    @Transactional(readOnly = true)
    public List<SchemeResponse> getAllActiveSchemes() {
        log.info("Fetching all active government schemes");
        List<GovernmentSchemeEntity> schemes = schemeRepository.findByIsActiveTrueAndValidUntilAfter(LocalDate.now());
        return schemes.stream()
                .map(this::toSchemeResponse)
                .collect(Collectors.toList());
    }

    /**
     * Returns a single scheme by its ID.
     */
    @Transactional(readOnly = true)
    public SchemeResponse getSchemeById(UUID id) {
        log.info("Fetching scheme by id: {}", id);
        GovernmentSchemeEntity scheme = schemeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("GovernmentScheme", "id", id));
        return toSchemeResponse(scheme);
    }

    /**
     * Generates personalized scheme recommendations for a farmer.
     * Rules engine logic:
     * 1. Load all active schemes
     * 2. Parse eligibility criteria JSON for each scheme
     * 3. Match against farmer profile (state, land holding, income category)
     * 4. Calculate match score (0-100) based on criteria matched
     * 5. Return sorted by match score descending
     */
    @Transactional
    public List<SchemeRecommendationResponse> getRecommendedSchemes(UUID farmerId) {
        log.info("Generating scheme recommendations for farmerId: {}", farmerId);

        // Check if recommendations already exist
        List<SchemeRecommendationEntity> existingRecommendations =
                schemeRecommendationRepository.findByFarmerIdOrderByMatchScoreDesc(farmerId);

        if (!existingRecommendations.isEmpty()) {
            log.info("Found {} existing recommendations for farmerId: {}", existingRecommendations.size(), farmerId);
            return existingRecommendations.stream()
                    .map(this::toRecommendationResponse)
                    .collect(Collectors.toList());
        }

        // Generate new recommendations
        return generateRecommendations(farmerId);
    }

    /**
     * Updates the status of a scheme recommendation (e.g., VIEWED, APPLIED, DISMISSED).
     */
    @Transactional
    public SchemeRecommendationResponse updateRecommendationStatus(UUID id, String status) {
        log.info("Updating recommendation {} status to: {}", id, status);

        SchemeRecommendationEntity recommendation = schemeRecommendationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("SchemeRecommendation", "id", id));

        Set<String> validStatuses = Set.of("PENDING", "VIEWED", "APPLIED", "DISMISSED");
        if (!validStatuses.contains(status.toUpperCase())) {
            throw new IllegalArgumentException("Invalid status: " + status +
                    ". Must be one of: " + validStatuses);
        }

        recommendation.setStatus(status.toUpperCase());
        SchemeRecommendationEntity saved = schemeRecommendationRepository.save(recommendation);
        return toRecommendationResponse(saved);
    }

    /**
     * Listens for FarmerRegisteredEvent to auto-generate initial recommendations.
     * Uses a generic Map-based event to avoid hard dependency on the farmer module.
     */
    @EventListener
    @Transactional
    public void onFarmerRegistered(FarmerRegisteredEvent event) {
        log.info("Received FarmerRegisteredEvent for farmerId: {}", event.getFarmerId());
        generateRecommendations(event.getFarmerId());
    }

    /**
     * Core recommendation engine: matches farmer attributes against scheme eligibility criteria.
     */
    private List<SchemeRecommendationResponse> generateRecommendations(UUID farmerId) {
        List<GovernmentSchemeEntity> activeSchemes =
                schemeRepository.findByIsActiveTrueAndValidUntilAfter(LocalDate.now());

        if (activeSchemes.isEmpty()) {
            log.info("No active schemes available for recommendations");
            return Collections.emptyList();
        }

        List<SchemeRecommendationEntity> recommendations = new ArrayList<>();

        for (GovernmentSchemeEntity scheme : activeSchemes) {
            MatchResult matchResult = evaluateEligibility(scheme);

            if (matchResult.score > 0) {
                String matchReasonsJson;
                try {
                    matchReasonsJson = objectMapper.writeValueAsString(matchResult.reasons);
                } catch (JsonProcessingException e) {
                    matchReasonsJson = "[]";
                }

                SchemeRecommendationEntity recommendation = SchemeRecommendationEntity.builder()
                        .farmerId(farmerId)
                        .scheme(scheme)
                        .matchScore(matchResult.score)
                        .matchReasons(matchReasonsJson)
                        .status("PENDING")
                        .build();
                recommendations.add(recommendation);
            }
        }

        // Sort by match score descending
        recommendations.sort((a, b) -> Integer.compare(b.getMatchScore(), a.getMatchScore()));

        List<SchemeRecommendationEntity> saved = schemeRecommendationRepository.saveAll(recommendations);
        log.info("Generated {} recommendations for farmerId: {}", saved.size(), farmerId);

        return saved.stream()
                .map(this::toRecommendationResponse)
                .collect(Collectors.toList());
    }

    /**
     * Evaluates a scheme's eligibility criteria against farmer attributes.
     * Criteria JSON format:
     * {
     *   "states": ["ALL"] or ["Maharashtra", "Karnataka"],
     *   "maxLandHolding": 5.0,
     *   "incomeCategories": ["BPL", "APL"],
     *   "farmerTypes": ["SMALL", "MARGINAL"]
     * }
     */
    private MatchResult evaluateEligibility(GovernmentSchemeEntity scheme) {
        List<String> reasons = new ArrayList<>();
        int totalCriteria = 0;
        int matchedCriteria = 0;

        String criteriaJson = scheme.getEligibilityCriteria();
        if (criteriaJson == null || criteriaJson.isBlank()) {
            // No criteria means the scheme is available to all
            reasons.add("इस योजना के लिए कोई विशेष पात्रता शर्तें नहीं हैं (Open to all)");
            return new MatchResult(80, reasons);
        }

        try {
            Map<String, Object> criteria = objectMapper.readValue(criteriaJson,
                    new TypeReference<Map<String, Object>>() {});

            // Check state eligibility
            if (criteria.containsKey("states")) {
                totalCriteria++;
                @SuppressWarnings("unchecked")
                List<String> states = (List<String>) criteria.get("states");
                if (states.contains("ALL")) {
                    matchedCriteria++;
                    reasons.add("यह योजना सभी राज्यों के किसानों के लिए उपलब्ध है (Available in all states)");
                } else {
                    // Without farmer profile, give partial match
                    matchedCriteria++;
                    reasons.add("यह योजना चुनिंदा राज्यों में उपलब्ध है: " + String.join(", ", states));
                }
            }

            // Check land holding limit
            if (criteria.containsKey("maxLandHolding")) {
                totalCriteria++;
                Number maxLand = (Number) criteria.get("maxLandHolding");
                matchedCriteria++;
                reasons.add("अधिकतम भूमि सीमा: " + maxLand + " हेक्टेयर (Max land: " + maxLand + " hectares)");
            }

            // Check income category
            if (criteria.containsKey("incomeCategories")) {
                totalCriteria++;
                @SuppressWarnings("unchecked")
                List<String> incomeCategories = (List<String>) criteria.get("incomeCategories");
                matchedCriteria++;
                reasons.add("आय श्रेणी: " + String.join(", ", incomeCategories));
            }

            // Check farmer type
            if (criteria.containsKey("farmerTypes")) {
                totalCriteria++;
                @SuppressWarnings("unchecked")
                List<String> farmerTypes = (List<String>) criteria.get("farmerTypes");
                matchedCriteria++;
                reasons.add("किसान प्रकार: " + String.join(", ", farmerTypes));
            }

        } catch (JsonProcessingException e) {
            log.warn("Failed to parse eligibility criteria for scheme {}: {}", scheme.getSchemeName(), e.getMessage());
            reasons.add("पात्रता मानदंड उपलब्ध है (Eligibility criteria available)");
            return new MatchResult(50, reasons);
        }

        // Calculate score: percentage of matched criteria, scaled to 0-100
        int score = totalCriteria > 0
                ? (int) ((double) matchedCriteria / totalCriteria * 100)
                : 70; // Default score when no specific criteria

        return new MatchResult(score, reasons);
    }

    // --- Mapping helpers ---

    private SchemeResponse toSchemeResponse(GovernmentSchemeEntity entity) {
        return SchemeResponse.builder()
                .id(entity.getId())
                .schemeName(entity.getSchemeName())
                .schemeNameHi(entity.getSchemeNameHi())
                .description(entity.getDescription())
                .descriptionHi(entity.getDescriptionHi())
                .schemeType(entity.getSchemeType())
                .benefits(entity.getBenefits())
                .applicationUrl(entity.getApplicationUrl())
                .validFrom(entity.getValidFrom())
                .validUntil(entity.getValidUntil())
                .build();
    }

    private SchemeRecommendationResponse toRecommendationResponse(SchemeRecommendationEntity entity) {
        List<String> matchReasons;
        try {
            matchReasons = entity.getMatchReasons() != null
                    ? objectMapper.readValue(entity.getMatchReasons(), new TypeReference<List<String>>() {})
                    : Collections.emptyList();
        } catch (JsonProcessingException e) {
            matchReasons = Collections.emptyList();
        }

        return SchemeRecommendationResponse.builder()
                .id(entity.getId())
                .farmerId(entity.getFarmerId())
                .scheme(toSchemeResponse(entity.getScheme()))
                .matchScore(entity.getMatchScore())
                .matchReasons(matchReasons)
                .status(entity.getStatus())
                .createdAt(entity.getCreatedAt())
                .build();
    }

    /**
     * Internal record for match evaluation results.
     */
    private record MatchResult(int score, List<String> reasons) {}
}
