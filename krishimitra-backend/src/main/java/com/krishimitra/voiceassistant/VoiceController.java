package com.krishimitra.voiceassistant;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.krishimitra.shared.dto.ApiResponse;
import com.krishimitra.voiceassistant.dto.TranscriptionResponse;
import com.krishimitra.voiceassistant.dto.VoiceChatRequest;
import com.krishimitra.voiceassistant.dto.VoiceChatResponse;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.krishimitra.translation.TranslationService;
import com.krishimitra.translation.dto.TranslationRequest;
import com.krishimitra.translation.dto.TranslationResponse;
import java.io.IOException;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/v1/voice")
@RequiredArgsConstructor
public class VoiceController {

    private final VoiceAssistantService voiceAssistantService;
    private final AdvisoryOrchestrator  advisoryOrchestrator;
    private final TranslationService    translationService;

    @PostMapping(value = "/transcribe", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<TranscriptionResponse>> transcribe(
            @RequestParam("audio") MultipartFile audio,
            @RequestParam(value = "language", defaultValue = "hi") String language) {

        log.info("Request received to transcribe audio file. Language: {}", language);
        try {
            byte[] audioBytes = audio.getBytes();
            TranscriptionResponse response = voiceAssistantService.transcribe(audioBytes, language);
            return ResponseEntity.ok(ApiResponse.success("Audio transcribed successfully", response));
        } catch (IOException e) {
            log.error("Failed to read audio file bytes", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to read audio file: " + e.getMessage()));
        }
    }

    @PostMapping("/synthesize")
    public ResponseEntity<byte[]> synthesize(
            @RequestParam("text") String text,
            @RequestParam(value = "language", defaultValue = "hi") String language) {

        log.info("Request received to synthesize speech. Text: {}, Language: {}", text, language);
        byte[] audioBytes = voiceAssistantService.synthesize(text, language);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.valueOf("audio/wav"));
        headers.setContentLength(audioBytes.length);

        return new ResponseEntity<>(audioBytes, headers, HttpStatus.OK);
    }

    @PostMapping(value = "/chat", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<VoiceChatResponse>> chat(
            @RequestParam("audio") MultipartFile audio,
            @RequestParam(value = "language", defaultValue = "hi") String language,
            @RequestParam(value = "farmerId", required = false) UUID farmerId) {

        log.info("Request received for full voice advisory chat. Language: {}, FarmerId: {}", language, farmerId);
        try {
            byte[] audioBytes = audio.getBytes();
            VoiceChatResponse response = voiceAssistantService.handleVoiceChat(audioBytes, language, farmerId);
            return ResponseEntity.ok(ApiResponse.success("Voice chat response generated", response));
        } catch (IOException e) {
            log.error("Failed to read voice chat input audio", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to read voice chat input: " + e.getMessage()));
        }
    }

    /**
     * Text-based advisory chat — does NOT require the AI sidecar.
     * The frontend sends already-transcribed text (from browser Web Speech API)
     * and gets an advisory response back. This is the primary flow when
     * the Python sidecar is not running.
     */
    @PostMapping(value = "/text-chat", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ApiResponse<VoiceChatResponse>> textChat(@RequestBody TextChatRequest request) {
        String queryText = request.getQueryText() != null ? request.getQueryText() : "";
        String language  = request.getLanguage()  != null ? request.getLanguage()  : "hi";
        UUID   farmerId  = request.getFarmerId();

        log.info("Text-chat request: language={}, farmerId={}, query={}", language, farmerId, queryText);

        String intent       = advisoryOrchestrator.detectIntent(queryText);
        // Generate advisory directly in the target language (no translation needed)
        String advisoryText = advisoryOrchestrator.generateAdvisory(queryText, intent, farmerId, language);

        VoiceChatResponse response = VoiceChatResponse.builder()
                .transcribedText(queryText)
                .responseText(advisoryText)
                .advisoryType(intent)
                .language(language)
                .audioBase64(null)  // TTS handled by the browser
                .build();

        return ResponseEntity.ok(ApiResponse.success("Advisory generated", response));
    }

    // DTO for the text-chat endpoint
    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class TextChatRequest {
        private String queryText;
        private String language;
        private UUID   farmerId;
    }
}
