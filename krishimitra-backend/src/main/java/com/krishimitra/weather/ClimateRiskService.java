package com.krishimitra.weather;

import com.krishimitra.farm.internal.FarmEntity;
import com.krishimitra.farm.internal.FarmRepository;
import com.krishimitra.shared.exception.ResourceNotFoundException;
import com.krishimitra.weather.dto.ClimateRiskResponse;
import com.krishimitra.weather.dto.ClimateRiskResponse.RiskDetail;
import com.krishimitra.weather.dto.ClimateRiskResponse.RiskLevel;
import com.krishimitra.weather.dto.ForecastDay;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * Analyzes climate risk for a farm based on 7-day weather forecast.
 * Evaluates drought, flood, heat stress, and water stress risks.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ClimateRiskService {

    private final FarmRepository farmRepository;
    private final WeatherService weatherService;

    /**
     * Analyze climate risks for a farm using 7-day forecast data.
     *
     * @param farmId the UUID of the farm
     * @return climate risk analysis with individual risk scores
     */
    public ClimateRiskResponse analyzeRisk(UUID farmId) {
        FarmEntity farm = farmRepository.findById(farmId)
                .orElseThrow(() -> new ResourceNotFoundException("Farm", "id", farmId));

        if (farm.getLatitude() == null || farm.getLongitude() == null) {
            throw new com.krishimitra.shared.exception.BadRequestException(
                    "Farm location (latitude/longitude) is required for climate risk analysis.");
        }

        log.info("Analyzing climate risk for farm {} at lat={}, lon={}", farmId, farm.getLatitude(), farm.getLongitude());

        List<ForecastDay> forecast = weatherService.getForecast(farm.getLatitude(), farm.getLongitude(), 7);

        RiskDetail droughtRisk = analyzeDroughtRisk(forecast);
        RiskDetail floodRisk = analyzeFloodRisk(forecast);
        RiskDetail heatStressRisk = analyzeHeatStressRisk(forecast);
        RiskDetail waterStressRisk = analyzeWaterStressRisk(forecast, droughtRisk, floodRisk);

        RiskLevel overallRisk = calculateOverallRisk(droughtRisk, floodRisk, heatStressRisk, waterStressRisk);

        return ClimateRiskResponse.builder()
                .farmId(farmId)
                .overallRisk(overallRisk)
                .droughtRisk(droughtRisk)
                .floodRisk(floodRisk)
                .heatStressRisk(heatStressRisk)
                .waterStressRisk(waterStressRisk)
                .analysisDate(LocalDate.now())
                .forecastDays(forecast.size())
                .build();
    }

    /**
     * Drought risk: total rainfall < 10mm in 7 days AND avg humidity low.
     */
    private RiskDetail analyzeDroughtRisk(List<ForecastDay> forecast) {
        double totalRainfall = forecast.stream()
                .mapToDouble(d -> d.getPrecipitationSum() != null ? d.getPrecipitationSum() : 0.0)
                .sum();

        double avgPrecipProb = forecast.stream()
                .mapToDouble(d -> d.getPrecipitationProbability() != null ? d.getPrecipitationProbability() : 50.0)
                .average()
                .orElse(50.0);

        int score;
        RiskLevel level;
        String explanation;
        String explanationHi;

        if (totalRainfall < 5 && avgPrecipProb < 20) {
            score = 90;
            level = RiskLevel.CRITICAL;
            explanation = "Severe drought conditions expected. Total rainfall < 5mm in 7 days with very low precipitation probability. Immediate irrigation recommended.";
            explanationHi = "गंभीर सूखे की स्थिति की उम्मीद। 7 दिनों में कुल वर्षा 5 मिमी से कम और वर्षा की संभावना बहुत कम। तत्काल सिंचाई की सिफारिश।";
        } else if (totalRainfall < 10 && avgPrecipProb < 30) {
            score = 70;
            level = RiskLevel.HIGH;
            explanation = "High drought risk. Very low rainfall expected over the next 7 days. Plan supplemental irrigation.";
            explanationHi = "सूखे का उच्च खतरा। अगले 7 दिनों में बहुत कम वर्षा की उम्मीद। पूरक सिंचाई की योजना बनाएं।";
        } else if (totalRainfall < 20) {
            score = 40;
            level = RiskLevel.MODERATE;
            explanation = "Moderate drought risk. Below-average rainfall expected. Monitor soil moisture levels.";
            explanationHi = "मध्यम सूखे का खतरा। औसत से कम वर्षा की उम्मीद। मिट्टी की नमी के स्तर की निगरानी करें।";
        } else {
            score = 15;
            level = RiskLevel.LOW;
            explanation = "Low drought risk. Adequate rainfall expected in the coming week.";
            explanationHi = "सूखे का कम खतरा। आने वाले सप्ताह में पर्याप्त वर्षा की उम्मीद।";
        }

        return RiskDetail.builder()
                .level(level)
                .score(score)
                .explanation(explanation)
                .explanationHi(explanationHi)
                .build();
    }

    /**
     * Flood risk: any day precipitation > 50mm OR total > 150mm.
     */
    private RiskDetail analyzeFloodRisk(List<ForecastDay> forecast) {
        double totalRainfall = forecast.stream()
                .mapToDouble(d -> d.getPrecipitationSum() != null ? d.getPrecipitationSum() : 0.0)
                .sum();

        double maxDailyRainfall = forecast.stream()
                .mapToDouble(d -> d.getPrecipitationSum() != null ? d.getPrecipitationSum() : 0.0)
                .max()
                .orElse(0.0);

        int score;
        RiskLevel level;
        String explanation;
        String explanationHi;

        if (maxDailyRainfall > 100 || totalRainfall > 250) {
            score = 95;
            level = RiskLevel.CRITICAL;
            explanation = String.format("Critical flood risk! Peak daily rainfall %.1fmm expected. Total 7-day rainfall %.1fmm. Protect crops and ensure drainage.", maxDailyRainfall, totalRainfall);
            explanationHi = String.format("बाढ़ का गंभीर खतरा! अधिकतम दैनिक वर्षा %.1f मिमी और कुल 7-दिन वर्षा %.1f मिमी। फसलों की रक्षा करें और जल निकासी सुनिश्चित करें।", maxDailyRainfall, totalRainfall);
        } else if (maxDailyRainfall > 50 || totalRainfall > 150) {
            score = 75;
            level = RiskLevel.HIGH;
            explanation = String.format("High flood risk. Heavy rainfall expected (peak: %.1fmm/day, total: %.1fmm). Check drainage systems.", maxDailyRainfall, totalRainfall);
            explanationHi = String.format("बाढ़ का उच्च खतरा। भारी वर्षा की उम्मीद (अधिकतम: %.1f मिमी/दिन, कुल: %.1f मिमी)। जल निकासी प्रणाली की जांच करें।", maxDailyRainfall, totalRainfall);
        } else if (maxDailyRainfall > 30 || totalRainfall > 100) {
            score = 45;
            level = RiskLevel.MODERATE;
            explanation = "Moderate flood risk. Above-average rainfall expected. Ensure field drainage is clear.";
            explanationHi = "मध्यम बाढ़ का खतरा। औसत से अधिक वर्षा की उम्मीद। सुनिश्चित करें कि खेत की जल निकासी साफ है।";
        } else {
            score = 10;
            level = RiskLevel.LOW;
            explanation = "Low flood risk. Normal rainfall levels expected.";
            explanationHi = "बाढ़ का कम खतरा। सामान्य वर्षा स्तर की उम्मीद।";
        }

        return RiskDetail.builder()
                .level(level)
                .score(score)
                .explanation(explanation)
                .explanationHi(explanationHi)
                .build();
    }

    /**
     * Heat stress: any day max temp > 42°C.
     */
    private RiskDetail analyzeHeatStressRisk(List<ForecastDay> forecast) {
        double maxTemp = forecast.stream()
                .mapToDouble(d -> d.getTempMax() != null ? d.getTempMax() : 30.0)
                .max()
                .orElse(30.0);

        double avgMaxTemp = forecast.stream()
                .mapToDouble(d -> d.getTempMax() != null ? d.getTempMax() : 30.0)
                .average()
                .orElse(30.0);

        int hotDays = (int) forecast.stream()
                .filter(d -> d.getTempMax() != null && d.getTempMax() > 38)
                .count();

        int score;
        RiskLevel level;
        String explanation;
        String explanationHi;

        if (maxTemp > 45 || hotDays >= 5) {
            score = 92;
            level = RiskLevel.CRITICAL;
            explanation = String.format("Critical heat stress! Maximum temperature %.1f°C with %d days above 38°C. Provide shade and extra water to crops immediately.", maxTemp, hotDays);
            explanationHi = String.format("गंभीर लू का खतरा! अधिकतम तापमान %.1f°C, %d दिन 38°C से ऊपर। तुरंत फसलों को छाया और अतिरिक्त पानी दें।", maxTemp, hotDays);
        } else if (maxTemp > 42 || hotDays >= 3) {
            score = 72;
            level = RiskLevel.HIGH;
            explanation = String.format("High heat stress risk. Maximum temperature reaching %.1f°C. Increase irrigation frequency and consider mulching.", maxTemp);
            explanationHi = String.format("लू का उच्च खतरा। अधिकतम तापमान %.1f°C तक। सिंचाई की आवृत्ति बढ़ाएं और मल्चिंग पर विचार करें।", maxTemp);
        } else if (maxTemp > 38 || avgMaxTemp > 35) {
            score = 42;
            level = RiskLevel.MODERATE;
            explanation = "Moderate heat stress. Above-normal temperatures expected. Monitor crop wilting.";
            explanationHi = "मध्यम लू का खतरा। सामान्य से अधिक तापमान की उम्मीद। फसल के मुरझाने पर निगरानी रखें।";
        } else {
            score = 12;
            level = RiskLevel.LOW;
            explanation = "Low heat stress risk. Temperatures within normal range.";
            explanationHi = "लू का कम खतरा। तापमान सामान्य सीमा में।";
        }

        return RiskDetail.builder()
                .level(level)
                .score(score)
                .explanation(explanation)
                .explanationHi(explanationHi)
                .build();
    }

    /**
     * Water stress: combined analysis of drought and flood risks.
     */
    private RiskDetail analyzeWaterStressRisk(List<ForecastDay> forecast, RiskDetail droughtRisk, RiskDetail floodRisk) {
        double totalRainfall = forecast.stream()
                .mapToDouble(d -> d.getPrecipitationSum() != null ? d.getPrecipitationSum() : 0.0)
                .sum();

        double avgMinTemp = forecast.stream()
                .mapToDouble(d -> d.getTempMin() != null ? d.getTempMin() : 20.0)
                .average()
                .orElse(20.0);

        // Water stress combines drought risk with evapotranspiration estimate
        int combinedScore = (droughtRisk.getScore() + floodRisk.getScore()) / 2;

        // Adjust for high temps increasing evapotranspiration
        if (avgMinTemp > 28) {
            combinedScore = Math.min(100, combinedScore + 15);
        }

        // Adjust: very low rainfall with high temps is worse
        if (totalRainfall < 10 && avgMinTemp > 25) {
            combinedScore = Math.min(100, combinedScore + 20);
        }

        RiskLevel level;
        String explanation;
        String explanationHi;

        if (combinedScore >= 75) {
            level = RiskLevel.CRITICAL;
            explanation = "Critical water stress. Crop water requirements are severely unmet. Implement emergency irrigation and water conservation measures.";
            explanationHi = "गंभीर जल तनाव। फसल की पानी की जरूरतें गंभीर रूप से पूरी नहीं हो रही हैं। आपातकालीन सिंचाई और जल संरक्षण उपाय लागू करें।";
        } else if (combinedScore >= 50) {
            level = RiskLevel.HIGH;
            explanation = "High water stress risk. Crops may not receive adequate water. Plan irrigation scheduling.";
            explanationHi = "जल तनाव का उच्च खतरा। फसलों को पर्याप्त पानी नहीं मिल सकता। सिंचाई शेड्यूलिंग की योजना बनाएं।";
        } else if (combinedScore >= 30) {
            level = RiskLevel.MODERATE;
            explanation = "Moderate water stress. Monitor soil moisture and be prepared for supplemental irrigation.";
            explanationHi = "मध्यम जल तनाव। मिट्टी की नमी की निगरानी करें और पूरक सिंचाई के लिए तैयार रहें।";
        } else {
            level = RiskLevel.LOW;
            explanation = "Low water stress. Current and forecast conditions are favorable for crop water needs.";
            explanationHi = "जल तनाव कम। वर्तमान और पूर्वानुमान स्थितियाँ फसल की पानी की जरूरतों के लिए अनुकूल हैं।";
        }

        return RiskDetail.builder()
                .level(level)
                .score(combinedScore)
                .explanation(explanation)
                .explanationHi(explanationHi)
                .build();
    }

    /**
     * Calculate overall risk as the highest individual risk level.
     */
    private RiskLevel calculateOverallRisk(RiskDetail... risks) {
        RiskLevel highest = RiskLevel.LOW;
        for (RiskDetail risk : risks) {
            if (risk.getLevel().ordinal() > highest.ordinal()) {
                highest = risk.getLevel();
            }
        }
        return highest;
    }
}
