package com.krishimitra.voiceassistant.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response DTO for the voice chat endpoint.
 * Contains the full pipeline result: transcription, advisory text, and synthesized audio.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VoiceChatResponse {

    private String transcribedText;
    private String responseText;
    private String advisoryType;
    private String language;
    private String audioBase64;
}
