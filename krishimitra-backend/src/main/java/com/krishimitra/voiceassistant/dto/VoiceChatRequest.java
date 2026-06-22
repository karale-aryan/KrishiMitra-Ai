package com.krishimitra.voiceassistant.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * Request DTO for the voice chat endpoint.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VoiceChatRequest {

    private String language;
    private UUID farmerId;
}
