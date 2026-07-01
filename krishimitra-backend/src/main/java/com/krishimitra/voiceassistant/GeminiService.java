package com.krishimitra.voiceassistant;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.netty.http.client.HttpClient;

import jakarta.annotation.PostConstruct;
import java.time.Duration;
import java.util.List;
import java.util.Map;

/**
 * Full AI assistant service powered by Google Gemini with Google Search grounding.
 * Handles ANY user question — farming, weather, market prices, general knowledge.
 * Uses Google Search to provide real-time, accurate information.
 */
@Slf4j
@Service
public class GeminiService {

    private static final String GEMINI_API_URL = "https://generativelanguage.googleapis.com/v1beta/models/";

    @Value("${krishimitra.gemini.api-key:}")
    private String apiKey;

    @Value("${krishimitra.gemini.model:gemini-2.0-flash}")
    private String model;

    @Value("${krishimitra.gemini.max-tokens:1024}")
    private int maxTokens;

    @Value("${krishimitra.gemini.temperature:0.7}")
    private double temperature;

    @Value("${krishimitra.gemini.connect-timeout-ms:10000}")
    private int connectTimeoutMs;

    @Value("${krishimitra.gemini.read-timeout-ms:30000}")
    private int readTimeoutMs;

    private WebClient webClient;
    private boolean enabled;

    private static final String SYSTEM_PROMPT = """
            You are KrishiMitra AI Assistant — a smart, friendly, and knowledgeable AI helper
            built for Indian farmers and rural communities. You can answer ANY question,
            but you specialize in agriculture, farming, weather, market prices, and government schemes.

            CAPABILITIES:
            - Answer farming questions: crops, soil, irrigation, fertilizers, pesticides, harvest timing
            - Provide real-time info: weather, market prices (mandi bhav), government schemes
            - General knowledge: you can answer any question the user asks
            - Conversational: greet users warmly, have natural conversations

            LANGUAGE RULES:
            1. ALWAYS respond in the language specified. Use the native script:
               - "hi" = Hindi (देवनागरी)
               - "en" = English
               - "mr" = Marathi (देवनागरी)
               - "te" = Telugu (తెలుగు లిపి)
               - "kn" = Kannada (ಕನ್ನಡ ಲಿಪಿ)
               - "gu" = Gujarati (ગુજરાતી લિપિ)
               - "pa" = Punjabi (ਗੁਰਮੁਖੀ)
               - "ta" = Tamil (தமிழ் எழுத்து)
            2. If the user writes in a specific language, match that language regardless of the code.

            RESPONSE STYLE:
            1. Be concise but informative — farmers are busy people
            2. DO NOT use markdown formatting like ** or * or #. Use plain text only. Use numbered lists if needed.
            3. Be practical — give actionable advice
            4. Address farmers respectfully (किसान भाई, రైతు సోదరా, ರೈತ ಬಂಧು, शेतकरी बंधू, ખેડૂત ભાઈ, ਕਿਸਾਨ ਵੀਰ, விவசாயி நண்பரே)
            5. When discussing chemicals/pesticides, always mention safety precautions
            6. For market prices, mention the source and date if available
            7. Keep responses under 200 words unless the question requires detail
            8. You are allowed to and SHOULD provide direct URLs/links (like YouTube links or website URLs) when the user asks for them. Do NOT refuse to send links.
            """;

    @PostConstruct
    void init() {
        enabled = apiKey != null && !apiKey.isBlank();
        if (enabled) {
            HttpClient httpClient = HttpClient.create()
                    .responseTimeout(Duration.ofMillis(readTimeoutMs))
                    .option(io.netty.channel.ChannelOption.CONNECT_TIMEOUT_MILLIS, connectTimeoutMs);

            webClient = WebClient.builder()
                    .clientConnector(new ReactorClientHttpConnector(httpClient))
                    .codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(2 * 1024 * 1024))
                    .build();

            log.info("GeminiService initialized with model [{}] + Google Search. AI assistant ready.", model);
        } else {
            log.warn("GeminiService: No GEMINI_API_KEY configured. AI assistant will be unavailable.");
        }
    }

    /**
     * Returns true if the Gemini API is configured and available.
     */
    public boolean isEnabled() {
        return enabled;
    }

    /**
     * Generates an AI response for any user query using Gemini + Google Search.
     *
     * @param userQuery the user's question in any language
     * @param intent    detected intent category (for context)
     * @param language  target language code (hi, en, mr, te, kn)
     * @return AI-generated response text, or null if the call fails
     */
    public String generateResponse(String userQuery, String intent, String language, java.util.List<VoiceController.ChatMessage> history) {
        if (!enabled) {
            return null;
        }

        if (userQuery == null || userQuery.isBlank()) {
            return getGreeting(language);
        }

        try {
            String contextPrompt = String.format(
                    "Respond in language: %s\nUser's question: %s",
                    language, userQuery
            );

            java.util.List<Map<String, Object>> contents = new java.util.ArrayList<>();
            if (history != null) {
                for (VoiceController.ChatMessage msg : history) {
                    if (msg.getText() != null && !msg.getText().isBlank()) {
                        String role = "model".equalsIgnoreCase(msg.getRole()) || "assistant".equalsIgnoreCase(msg.getRole()) ? "model" : "user";
                        contents.add(Map.of("role", role, "parts", java.util.List.of(Map.of("text", msg.getText()))));
                    }
                }
            }
            contents.add(Map.of("role", "user", "parts", java.util.List.of(Map.of("text", contextPrompt))));

            // Build Gemini API request with Google Search grounding
            Map<String, Object> requestBody = Map.of(
                    "contents", contents,
                    "systemInstruction", Map.of(
                            "parts", List.of(Map.of("text", SYSTEM_PROMPT))
                    ),
                    "tools", List.of(
                            Map.of("google_search", Map.of())
                    ),
                    "generationConfig", Map.of(
                            "temperature", temperature,
                            "maxOutputTokens", maxTokens,
                            "topP", 0.95
                    )
            );

            String url = GEMINI_API_URL + model + ":generateContent?key=" + apiKey;

            GeminiResponse response = webClient.post()
                    .uri(url)
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(GeminiResponse.class)
                    .block();

            if (response != null && response.getCandidates() != null && !response.getCandidates().isEmpty()) {
                GeminiCandidate candidate = response.getCandidates().get(0);
                if (candidate.getContent() != null && candidate.getContent().getParts() != null
                        && !candidate.getContent().getParts().isEmpty()) {
                    // Collect all text parts (Gemini may split response across multiple parts)
                    StringBuilder sb = new StringBuilder();
                    for (GeminiPart part : candidate.getContent().getParts()) {
                        if (part.getText() != null) {
                            sb.append(part.getText());
                        }
                    }
                    String text = sb.toString().trim();
                    if (!text.isEmpty()) {
                        log.info("Gemini response: {} chars for query: {}",
                                text.length(), userQuery.substring(0, Math.min(60, userQuery.length())));
                        return text;
                    }
                }
            }

            log.warn("Gemini returned empty response for query: {}", userQuery);
            return null;

        } catch (WebClientResponseException e) {
            log.error("Gemini API error: {} - {}", e.getStatusCode(), e.getResponseBodyAsString());
            return null;
        } catch (Exception e) {
            log.error("Gemini API call failed: {}", e.getMessage(), e);
            return null;
        }
    }

    private String getGreeting(String language) {
        return switch (language) {
            case "en" -> "Hello! I'm KrishiMitra AI Assistant. Ask me anything about farming, weather, market prices, government schemes, or any other topic. How can I help you today?";
            case "mr" -> "\u0928\u092e\u0938\u094d\u0915\u093e\u0930! \u092e\u0940 \u0915\u0943\u0937\u093f\u092e\u093f\u0924\u094d\u0930 AI \u0938\u0939\u093e\u092f\u094d\u092f\u0915 \u0906\u0939\u0947. \u0936\u0947\u0924\u0940, \u0939\u0935\u093e\u092e\u093e\u0928, \u092c\u093e\u091c\u093e\u0930\u092d\u093e\u0935, \u0938\u0930\u0915\u093e\u0930\u0940 \u092f\u094b\u091c\u0928\u093e \u0915\u093f\u0902\u0935\u093e \u0915\u094b\u0923\u0924\u094d\u092f\u093e\u0939\u0940 \u0935\u093f\u0937\u092f\u093e\u0935\u0930 \u092e\u0932\u093e \u0935\u093f\u091a\u093e\u0930\u093e. \u0906\u091c \u092e\u0940 \u0924\u0941\u092e\u094d\u0939\u093e\u0932\u093e \u0915\u0936\u0940 \u092e\u0926\u0924 \u0915\u0930\u0942?";
            case "te" -> "\u0c28\u0c2e\u0c38\u0c4d\u0c15\u0c3e\u0c30\u0c02! \u0c28\u0c47\u0c28\u0c41 \u0c15\u0c43\u0c37\u0c3f\u0c2e\u0c3f\u0c24\u0c4d\u0c30 AI \u0c38\u0c39\u0c3e\u0c2f\u0c15\u0c41\u0c21\u0c3f\u0c28\u0c3f. \u0c35\u0c4d\u0c2f\u0c35\u0c38\u0c3e\u0c2f\u0c02, \u0c35\u0c3e\u0c24\u0c3e\u0c35\u0c30\u0c23\u0c02, \u0c2e\u0c3e\u0c30\u0c4d\u0c15\u0c46\u0c1f\u0c4d \u0c27\u0c30\u0c32\u0c41, \u0c2a\u0c4d\u0c30\u0c2d\u0c41\u0c24\u0c4d\u0c35 \u0c2a\u0c25\u0c15\u0c3e\u0c32\u0c41 \u0c32\u0c47\u0c26\u0c3e \u0c07\u0c24\u0c30 \u0c05\u0c02\u0c36\u0c3e\u0c32 \u0c17\u0c41\u0c30\u0c3f\u0c02\u0c1a\u0c3f \u0c28\u0c28\u0c4d\u0c28\u0c41 \u0c05\u0c21\u0c17\u0c02\u0c21\u0c3f. \u0c08\u0c30\u0c4b\u0c1c\u0c41 \u0c28\u0c47\u0c28\u0c41 \u0c2e\u0c40\u0c15\u0c41 \u0c0e\u0c32\u0c3e \u0c38\u0c39\u0c3e\u0c2f\u0c02 \u0c1a\u0c47\u0c2f\u0c17\u0c32\u0c28\u0c41?";
            case "kn" -> "\u0ca8\u0cae\u0cb8\u0ccd\u0c95\u0cbe\u0cb0! \u0ca8\u0cbe\u0ca8\u0cc1 \u0c95\u0cc3\u0cb7\u0cbf\u0cae\u0cbf\u0ca4\u0ccd\u0cb0 AI \u0cb8\u0cb9\u0cbe\u0caf\u0c95. \u0c95\u0cc3\u0cb7\u0cbf, \u0cb9\u0cb5\u0cbe\u0cae\u0cbe\u0ca8, \u0cae\u0cbe\u0cb0\u0cc1\u0c95\u0c9f\u0ccd\u0c9f\u0cc6 \u0ca7\u0cb0, \u0cb8\u0cb0\u0c95\u0cbe\u0cb0\u0cbf \u0caf\u0ccb\u0c9c\u0ca8\u0cc6\u0c97\u0cb3\u0cc1 \u0c85\u0ca5\u0cb5\u0cbe \u0caf\u0cbe\u0cb5\u0cc1\u0ca6\u0cc7 \u0cb5\u0cbf\u0cb7\u0caf\u0ca6 \u0cac\u0c97\u0ccd\u0c97\u0cc6 \u0ca8\u0ca8\u0ccd\u0ca8\u0ca8\u0ccd\u0ca8\u0cc1 \u0c95\u0cc7\u0cb3\u0cbf. \u0c88 \u0ca6\u0cbf\u0ca8 \u0ca8\u0cbf\u0cae\u0c97\u0cc6 \u0cb9\u0cc7\u0c97\u0cc6 \u0cb8\u0cb9\u0cbe\u0caf \u0cae\u0cbe\u0ca1\u0cb2\u0cbf?";
            case "gu" -> "\u0aa8\u0aae\u0ab8\u0acd\u0a95\u0abe\u0ab0! \u0ab9\u0ac1\u0a82 \u0a95\u0ac3\u0ab7\u0abf\u0aae\u0abf\u0aa4\u0acd\u0ab0 AI \u0ab8\u0ab9\u0abe\u0aaf\u0a95 \u0a9b\u0ac1\u0a82. \u0a96\u0ac7\u0aa4\u0ac0, \u0ab9\u0ab5\u0abe\u0aae\u0abe\u0aa8, \u0aac\u0a9c\u0abe\u0ab0\u0aad\u0abe\u0ab5, \u0ab8\u0ab0\u0a95\u0abe\u0ab0\u0ac0 \u0aaf\u0acb\u0a9c\u0aa8\u0abe \u0a95\u0ac7 \u0a95\u0acb\u0a88\u0aaa\u0aa3 \u0ab5\u0abf\u0ab7\u0aaf \u0aaa\u0ab0 \u0aae\u0aa8\u0ac7 \u0aaa\u0ac2\u0a9b\u0acb. \u0a86\u0a9c\u0ac7 \u0ab9\u0ac1\u0a82 \u0aa4\u0aae\u0abe\u0ab0\u0ac0 \u0a95\u0ac7\u0ab5\u0ac0 \u0ab0\u0ac0\u0aa4\u0ac7 \u0aae\u0aa6\u0aa6 \u0a95\u0ab0\u0ac0 \u0ab6\u0a95\u0ac1\u0a82?";
            case "pa" -> "\u0a38\u0a24 \u0a38\u0a4d\u0a30\u0a40 \u0a05\u0a15\u0a3e\u0a32! \u0a2e\u0a48\u0a02 \u0a15\u0a43\u0a37\u0a3f\u0a2e\u0a3f\u0a24\u0a4d\u0a30 AI \u0a38\u0a39\u0a3e\u0a07\u0a15 \u0a39\u0a3e\u0a02\u0964 \u0a16\u0a47\u0a24\u0a40, \u0a2e\u0a4c\u0a38\u0a2e, \u0a2e\u0a70\u0a21\u0a40 \u0a2d\u0a3e\u0a05, \u0a38\u0a30\u0a15\u0a3e\u0a30\u0a40 \u0a38\u0a15\u0a40\u0a2e\u0a3e\u0a02 \u0a1c\u0a3e\u0a02 \u0a15\u0a4b\u0a08 \u0a35\u0a40 \u0a38\u0a35\u0a3e\u0a32 \u0a2a\u0a41\u0a71\u0a1b\u0a4b\u0964 \u0a05\u0a71\u0a1c \u0a2e\u0a48\u0a02 \u0a24\u0a41\u0a39\u0a3e\u0a21\u0a40 \u0a15\u0a40 \u0a2e\u0a26\u0a26 \u0a15\u0a30 \u0a38\u0a15\u0a26\u0a3e \u0a39\u0a3e\u0a02?";
            case "ta" -> "\u0bb5\u0ba3\u0b95\u0bcd\u0b95\u0bae\u0bcd! \u0ba8\u0bbe\u0ba9\u0bcd \u0b95\u0bbf\u0bb0\u0bbf\u0bb7\u0bbf\u0bae\u0bbf\u0ba4\u0bcd\u0bb0\u0bbe AI \u0b89\u0ba4\u0bb5\u0bbf\u0baf\u0bbe\u0bb3\u0bb0\u0bcd. \u0bb5\u0bc7\u0bb3\u0bbe\u0ba3\u0bcd\u0bae\u0bc8, \u0bb5\u0bbe\u0ba9\u0bbf\u0bb2\u0bc8, \u0b9a\u0ba8\u0bcd\u0ba4\u0bc8 \u0bb5\u0bbf\u0bb2\u0bc8, \u0b85\u0bb0\u0b9a\u0bc1 \u0ba4\u0bbf\u0b9f\u0bcd\u0b9f\u0b99\u0bcd\u0b95\u0bb3\u0bcd \u0b85\u0bb2\u0bcd\u0bb2\u0ba4\u0bc1 \u0bb5\u0bc7\u0bb1\u0bc1 \u0b8e\u0ba8\u0bcd\u0ba4 \u0ba4\u0bb2\u0bc8\u0baa\u0bcd\u0baa\u0bbf\u0bb2\u0bc1\u0bae\u0bcd \u0b95\u0bc7\u0bb3\u0bc1\u0b99\u0bcd\u0b95\u0bb3\u0bcd. \u0b87\u0ba9\u0bcd\u0bb1\u0bc1 \u0ba8\u0bbe\u0ba9\u0bcd \u0b89\u0b99\u0bcd\u0b95\u0bb3\u0bc1\u0b95\u0bcd\u0b95\u0bc1 \u0b8e\u0baa\u0bcd\u0baa\u0b9f\u0bbf \u0b89\u0ba4\u0bb5 \u0bae\u0bc1\u0b9f\u0bbf\u0baf\u0bc1\u0bae\u0bcd?";
            default -> "\u0928\u092e\u0938\u094d\u0924\u0947! \u092e\u0948\u0902 \u0915\u0943\u0937\u093f\u092e\u093f\u0924\u094d\u0930 AI \u0938\u0939\u093e\u092f\u0915 \u0939\u0942\u0901\u0964 \u0916\u0947\u0924\u0940, \u092e\u094c\u0938\u092e, \u092c\u093e\u091c\u093e\u0930 \u092d\u093e\u0935, \u0938\u0930\u0915\u093e\u0930\u0940 \u092f\u094b\u091c\u0928\u093e\u090f\u0901 \u092f\u093e \u0915\u094b\u0908 \u092d\u0940 \u0938\u0935\u093e\u0932 \u092a\u0942\u091b\u093f\u090f\u0964 \u0906\u091c \u092e\u0948\u0902 \u0906\u092a\u0915\u0940 \u0915\u094d\u092f\u093e \u092e\u0926\u0926 \u0915\u0930 \u0938\u0915\u0924\u093e \u0939\u0942\u0901?";
        };
    }

    // ── Gemini API Response DTOs ──────────────────────────────────────────────

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    static class GeminiResponse {
        private List<GeminiCandidate> candidates;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    static class GeminiCandidate {
        private GeminiContent content;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    static class GeminiContent {
        private List<GeminiPart> parts;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    static class GeminiPart {
        private String text;
    }
}
