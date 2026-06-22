package com.krishimitra.translation.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response DTO from the translation endpoint.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TranslationResponse {

    private String translatedText;
    private String sourceLanguage;
    private String targetLanguage;
    private String modelVersion;
}
