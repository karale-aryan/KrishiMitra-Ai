package com.krishimitra.voiceassistant;

import com.krishimitra.shared.exception.AIServiceException;
import com.krishimitra.voiceassistant.dto.TranscriptionResponse;
import com.krishimitra.voiceassistant.dto.VoiceChatResponse;
import com.krishimitra.voiceassistant.internal.AdvisoryLogEntity;
import com.krishimitra.voiceassistant.internal.AdvisoryLogRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.MediaType;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientRequestException;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import com.krishimitra.translation.TranslationService;
import com.krishimitra.translation.dto.TranslationRequest;
import com.krishimitra.translation.dto.TranslationResponse;
import java.util.Base64;
import java.util.Map;
import java.util.UUID;

/**
 * Service that coordinates with the Python AI sidecar for speech-to-text and text-to-speech,
 * and orchestrates the full voice chat pipeline.
 */
@Slf4j
@Service
public class VoiceAssistantService {

    private final WebClient aiSidecarClient;
    private final AdvisoryOrchestrator advisoryOrchestrator;
    private final AdvisoryLogRepository advisoryLogRepository;
    private final TranslationService translationService;

    public VoiceAssistantService(
            @Qualifier("aiSidecarClient") WebClient aiSidecarClient,
            AdvisoryOrchestrator advisoryOrchestrator,
            AdvisoryLogRepository advisoryLogRepository,
            TranslationService translationService) {
        this.aiSidecarClient = aiSidecarClient;
        this.advisoryOrchestrator = advisoryOrchestrator;
        this.advisoryLogRepository = advisoryLogRepository;
        this.translationService = translationService;
    }

    /**
     * Transcribes audio data to text via the AI sidecar's Whisper endpoint.
     *
     * @param audioData raw audio bytes (WAV/MP3)
     * @param language  BCP-47 language hint (e.g., "hi", "en")
     * @return transcription result with text, detected language, and confidence
     */
    public TranscriptionResponse transcribe(byte[] audioData, String language) {
        log.info("Sending audio ({} bytes) for transcription, language hint: {}", audioData.length, language);

        try {
            MultipartBodyBuilder bodyBuilder = new MultipartBodyBuilder();
            bodyBuilder.part("audio", new ByteArrayResource(audioData) {
                @Override
                public String getFilename() {
                    return "audio.wav";
                }
            }).contentType(MediaType.APPLICATION_OCTET_STREAM);
            bodyBuilder.part("language", language != null ? language : "hi");

            TranscriptionResponse response = aiSidecarClient.post()
                    .uri("/transcribe")
                    .contentType(MediaType.MULTIPART_FORM_DATA)
                    .body(BodyInserters.fromMultipartData(bodyBuilder.build()))
                    .retrieve()
                    .bodyToMono(TranscriptionResponse.class)
                    .block();

            log.info("Transcription complete: {}", response != null ? response.getText() : "null");
            return response;

        } catch (WebClientRequestException e) {
            log.error("AI sidecar unreachable for transcription: {}", e.getMessage());
            throw new AIServiceException("AI sidecar service is unavailable for speech-to-text. Please try again later.", e);
        } catch (WebClientResponseException e) {
            log.error("AI sidecar transcription error: {} - {}", e.getStatusCode(), e.getResponseBodyAsString());
            throw new AIServiceException("Speech-to-text service returned an error: " + e.getStatusCode(), e);
        }
    }

    /**
     * Synthesizes text to speech (WAV audio) via the AI sidecar's TTS endpoint.
     *
     * @param text     text to synthesize
     * @param language target language code
     * @return raw WAV audio bytes
     */
    public byte[] synthesize(String text, String language) {
        log.info("Synthesizing speech for text ({} chars), language: {}", text.length(), language);

        try {
            Map<String, String> requestBody = Map.of(
                    "text", text,
                    "language", language != null ? language : "hi"
            );

            byte[] audioBytes = aiSidecarClient.post()
                    .uri("/synthesize")
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(byte[].class)
                    .block();

            log.info("Speech synthesis complete: {} bytes", audioBytes != null ? audioBytes.length : 0);
            return audioBytes;

        } catch (WebClientRequestException e) {
            log.error("AI sidecar unreachable for synthesis: {}", e.getMessage());
            throw new AIServiceException("AI sidecar service is unavailable for text-to-speech. Please try again later.", e);
        } catch (WebClientResponseException e) {
            log.error("AI sidecar synthesis error: {} - {}", e.getStatusCode(), e.getResponseBodyAsString());
            throw new AIServiceException("Text-to-speech service returned an error: " + e.getStatusCode(), e);
        }
    }

    /**
     * Full voice chat pipeline: audio → STT → advisory → TTS → response.
     *
     * @param audioData raw audio bytes from the user
     * @param language  language hint
     * @param farmerId  optional farmer ID for personalization
     * @return complete voice chat response with text and base64-encoded audio
     */
    @Transactional
    public VoiceChatResponse handleVoiceChat(byte[] audioData, String language, UUID farmerId) {
        long startTime = System.currentTimeMillis();
        String effectiveLanguage = language != null ? language : "hi";

        log.info("Starting voice chat pipeline for farmerId: {}, language: {}", farmerId, effectiveLanguage);

        // Step 1: Transcribe audio to text
        TranscriptionResponse transcription = transcribe(audioData, effectiveLanguage);
        String transcribedText = transcription.getText();

        if (transcribedText == null || transcribedText.isBlank()) {
            transcribedText = "";
            log.warn("Empty transcription result, proceeding with general advisory");
        }

        // Step 2: Detect intent and generate advisory directly in the target language
        String intent = advisoryOrchestrator.detectIntent(transcribedText);
        String advisoryText = advisoryOrchestrator.generateAdvisory(transcribedText, intent, farmerId, effectiveLanguage);

        // Step 3: Synthesize advisory response to speech
        byte[] responseAudio;
        String audioBase64;
        try {
            responseAudio = synthesize(advisoryText, effectiveLanguage);
            audioBase64 = Base64.getEncoder().encodeToString(responseAudio);
        } catch (AIServiceException e) {
            log.warn("TTS synthesis failed, returning text-only response: {}", e.getMessage());
            audioBase64 = null;
        }

        long responseTimeMs = System.currentTimeMillis() - startTime;

        // Step 4: Log the interaction
        AdvisoryLogEntity logEntry = AdvisoryLogEntity.builder()
                .farmerId(farmerId)
                .advisoryType(intent)
                .queryText(transcribedText)
                .responseText(advisoryText)
                .queryLanguage(transcription.getDetectedLanguage() != null
                        ? transcription.getDetectedLanguage() : effectiveLanguage)
                .responseLanguage(effectiveLanguage)
                .inputMode("VOICE")
                .sessionId(UUID.randomUUID().toString())
                .responseTimeMs(responseTimeMs)
                .build();
        advisoryLogRepository.save(logEntry);

        log.info("Voice chat pipeline completed in {}ms, intent: {}", responseTimeMs, intent);

        // Step 5: Build and return response
        return VoiceChatResponse.builder()
                .transcribedText(transcribedText)
                .responseText(advisoryText)
                .advisoryType(intent)
                .language(effectiveLanguage)
                .audioBase64(audioBase64)
                .build();
    }
}
