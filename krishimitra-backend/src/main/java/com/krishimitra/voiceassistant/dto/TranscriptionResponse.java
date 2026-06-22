package com.krishimitra.voiceassistant.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response DTO from the AI sidecar speech-to-text transcription endpoint.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TranscriptionResponse {

    private String text;
    private String detectedLanguage;
    private Double confidence;
}
