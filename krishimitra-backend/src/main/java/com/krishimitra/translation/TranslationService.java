package com.krishimitra.translation;

import com.krishimitra.shared.exception.BadRequestException;
import com.krishimitra.translation.dto.TranslationRequest;
import com.krishimitra.translation.dto.TranslationResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientRequestException;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Service for translating text between supported Indian languages.
 * Uses the AI sidecar (IndicTrans2) when available, falls back to
 * a basic dictionary for common agricultural phrases in dev mode.
 */
@Slf4j
@Service
public class TranslationService {

    private static final Set<String> SUPPORTED_LANGUAGES = Set.of("en", "hi", "mr", "te", "kn");

    private final WebClient aiSidecarClient;

    // Simple fallback dictionary: en → hi
    private static final Map<String, String> EN_HI_DICT = new HashMap<>();
    static {
        EN_HI_DICT.put("hello", "नमस्ते");
        EN_HI_DICT.put("farmer", "किसान");
        EN_HI_DICT.put("crop", "फसल");
        EN_HI_DICT.put("weather", "मौसम");
        EN_HI_DICT.put("rain", "बारिश");
        EN_HI_DICT.put("soil", "मिट्टी");
        EN_HI_DICT.put("water", "पानी");
        EN_HI_DICT.put("seed", "बीज");
        EN_HI_DICT.put("harvest", "फसल कटाई");
        EN_HI_DICT.put("fertilizer", "उर्वरक");
        EN_HI_DICT.put("disease", "रोग");
        EN_HI_DICT.put("field", "खेत");
        EN_HI_DICT.put("farm", "खेत");
        EN_HI_DICT.put("temperature", "तापमान");
        EN_HI_DICT.put("humidity", "नमी");
        EN_HI_DICT.put("scheme", "योजना");
        EN_HI_DICT.put("government", "सरकार");
        EN_HI_DICT.put("irrigation", "सिंचाई");
        EN_HI_DICT.put("wheat", "गेहूं");
        EN_HI_DICT.put("rice", "चावल");
        EN_HI_DICT.put("cotton", "कपास");
        EN_HI_DICT.put("sugarcane", "गन्ना");
    }

    // hi → en (reverse)
    private static final Map<String, String> HI_EN_DICT = new HashMap<>();
    static {
        EN_HI_DICT.forEach((k, v) -> HI_EN_DICT.put(v, k));
    }

    public TranslationService(@Qualifier("aiSidecarClient") WebClient aiSidecarClient) {
        this.aiSidecarClient = aiSidecarClient;
    }

    /**
     * Translates text from one language to another via the AI sidecar's IndicTrans2 endpoint.
     * Falls back to a simple word-by-word dictionary translation when the sidecar is unavailable.
     *
     * @param request translation request with source text, source language, and target language
     * @return translation response with translated text
     */
    public TranslationResponse translate(TranslationRequest request) {
        log.info("Translating text from [{}] to [{}], length: {} chars",
                request.getSourceLanguage(), request.getTargetLanguage(), request.getSourceText().length());

        validateLanguages(request.getSourceLanguage(), request.getTargetLanguage());

        if (request.getSourceLanguage().equals(request.getTargetLanguage())) {
            log.info("Source and target languages are identical, returning original text");
            return TranslationResponse.builder()
                    .translatedText(request.getSourceText())
                    .sourceLanguage(request.getSourceLanguage())
                    .targetLanguage(request.getTargetLanguage())
                    .modelVersion("passthrough")
                    .build();
        }

        // Try the AI sidecar first
        try {
            Map<String, String> requestBody = Map.of(
                    "text", request.getSourceText(),
                    "source_language", request.getSourceLanguage(),
                    "target_language", request.getTargetLanguage()
            );

            TranslationResponse response = aiSidecarClient.post()
                    .uri("/translate")
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(TranslationResponse.class)
                    .block();

            if (response != null) {
                response.setSourceLanguage(request.getSourceLanguage());
                response.setTargetLanguage(request.getTargetLanguage());
            }

            log.info("Translation complete via AI sidecar: {} → {} ({} chars → {} chars)",
                    request.getSourceLanguage(), request.getTargetLanguage(),
                    request.getSourceText().length(),
                    response != null ? response.getTranslatedText().length() : 0);

            return response;

        } catch (WebClientRequestException e) {
            log.warn("AI sidecar unreachable for translation, using fallback dictionary: {}", e.getMessage());
            return fallbackTranslate(request);
        } catch (WebClientResponseException e) {
            log.warn("AI sidecar returned error for translation, using fallback: {} - {}",
                    e.getStatusCode(), e.getResponseBodyAsString());
            return fallbackTranslate(request);
        } catch (Exception e) {
            log.warn("Unexpected error contacting AI sidecar, using fallback: {}", e.getMessage());
            return fallbackTranslate(request);
        }
    }

    /**
     * Simple word-by-word dictionary fallback for when the AI sidecar is unavailable.
     * Supports en↔hi. For other language pairs, returns the source text with a note.
     */
    private TranslationResponse fallbackTranslate(TranslationRequest request) {
        String src = request.getSourceLanguage();
        String tgt = request.getTargetLanguage();
        String text = request.getSourceText();

        Map<String, String> dict = null;
        if ("en".equals(src) && "hi".equals(tgt)) {
            dict = EN_HI_DICT;
        } else if ("hi".equals(src) && "en".equals(tgt)) {
            dict = HI_EN_DICT;
        }

        String translated;
        if (dict != null) {
            // Word-by-word replacement (case-insensitive)
            translated = text;
            for (Map.Entry<String, String> entry : dict.entrySet()) {
                translated = translated.replaceAll("(?i)\\b" + java.util.regex.Pattern.quote(entry.getKey()) + "\\b",
                        entry.getValue());
            }
        } else {
            // No dictionary available for this pair — return original text gracefully
            translated = text;
        }

        log.info("Fallback translation complete: {} → {} (dict-based)", src, tgt);

        return TranslationResponse.builder()
                .translatedText(translated)
                .sourceLanguage(src)
                .targetLanguage(tgt)
                .modelVersion("fallback-dictionary-v1")
                .build();
    }

    private void validateLanguages(String sourceLanguage, String targetLanguage) {
        if (!SUPPORTED_LANGUAGES.contains(sourceLanguage)) {
            throw new BadRequestException(
                    "Unsupported source language: " + sourceLanguage +
                            ". Supported languages: " + SUPPORTED_LANGUAGES);
        }
        if (!SUPPORTED_LANGUAGES.contains(targetLanguage)) {
            throw new BadRequestException(
                    "Unsupported target language: " + targetLanguage +
                            ". Supported languages: " + SUPPORTED_LANGUAGES);
        }
    }
}
