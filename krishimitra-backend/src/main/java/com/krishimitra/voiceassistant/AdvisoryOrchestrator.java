package com.krishimitra.voiceassistant;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.UUID;

/**
 * Orchestrates voice/text queries by routing them to the Gemini AI service.
 * Keeps lightweight intent detection for analytics and logging purposes only.
 */
@Slf4j
@Service
public class AdvisoryOrchestrator {

    public static final String CROP_ADVISORY = "CROP_ADVISORY";
    public static final String WEATHER_ADVISORY = "WEATHER_ADVISORY";
    public static final String DISEASE_ADVISORY = "DISEASE_ADVISORY";
    public static final String SCHEME_ADVISORY = "SCHEME_ADVISORY";
    public static final String GENERAL_ADVISORY = "GENERAL_ADVISORY";

    private final GeminiService geminiService;

    public AdvisoryOrchestrator(GeminiService geminiService) {
        this.geminiService = geminiService;
    }

    /**
     * Detects the broad intent category from the user's query text.
     * Used for analytics/logging only — does NOT affect the AI response.
     */
    public String detectIntent(String queryText) {
        if (queryText == null || queryText.isBlank()) {
            return GENERAL_ADVISORY;
        }
        String lower = queryText.toLowerCase();

        if (containsAny(lower, "crop", "fasal", "kheti", "seed", "beej", "sowing",
                "harvest", "fertilizer", "pesticide", "plant", "wheat", "rice",
                "maize", "orange", "apple", "mango", "banana", "sugarcane",
                "cotton", "coffee", "tea", "groundnut", "soybean", "pulses",
                "vegetables", "grapes", "pomegranate", "millets",
                // Marathi
                "\u092a\u0940\u0915", "\u092c\u093f\u092f\u093e\u0923\u0947", "\u0916\u0924", "\u0936\u0947\u0924\u0940", "\u092a\u0947\u0930\u0923\u0940",
                // Telugu
                "\u0c2a\u0c02\u0c1f", "\u0c35\u0c3f\u0c24\u0c4d\u0c24\u0c28\u0c02", "\u0c0e\u0c30\u0c41\u0c35\u0c41", "\u0c35\u0c4d\u0c2f\u0c35\u0c38\u0c3e\u0c2f\u0c02",
                // Kannada
                "\u0cac\u0cc6\u0cb3\u0cc6", "\u0cac\u0cc0\u0c9c", "\u0c97\u0cca\u0cac\u0ccd\u0cac\u0cb0", "\u0c95\u0cc3\u0cb7\u0cbf",
                // Gujarati
                "\u0aaa\u0abe\u0a95", "\u0aac\u0ac0\u0a9c", "\u0a96\u0abe\u0aa4\u0ab0", "\u0a96\u0ac7\u0aa4\u0ac0",
                // Punjabi
                "\u0a2b\u0a38\u0a32", "\u0a2c\u0a40\u0a1c", "\u0a16\u0a3e\u0a26", "\u0a16\u0a47\u0a24\u0a40",
                // Tamil
                "\u0baa\u0baf\u0bbf\u0bb0\u0bcd", "\u0bb5\u0bbf\u0ba4\u0bc8", "\u0b89\u0bb0\u0bae\u0bcd", "\u0bb5\u0bc7\u0bb3\u0bbe\u0ba3\u0bcd\u0bae\u0bc8")) {
            return CROP_ADVISORY;
        }
        if (containsAny(lower, "weather", "mausam", "barish", "rain", "temperature",
                "hawa", "dhoop", "storm", "flood", "drought",
                // Marathi
                "\u0939\u0935\u093e\u092e\u093e\u0928", "\u092a\u093e\u090a\u0938", "\u0935\u093e\u0926\u0933",
                // Telugu
                "\u0c35\u0c3e\u0c24\u0c3e\u0c35\u0c30\u0c23\u0c02", "\u0c35\u0c30\u0c4d\u0c37\u0c02",
                // Kannada
                "\u0cb9\u0cb5\u0cbe\u0cae\u0cbe\u0ca8", "\u0cae\u0cb3\u0cc6",
                // Gujarati
                "\u0ab9\u0ab5\u0abe\u0aae\u0abe\u0aa8", "\u0ab5\u0ab0\u0ab8\u0abe\u0aa6",
                // Punjabi
                "\u0a2e\u0a4c\u0a38\u0a2e", "\u0a2e\u0a40\u0a02\u0a39",
                // Tamil
                "\u0bb5\u0bbe\u0ba9\u0bbf\u0bb2\u0bc8", "\u0bae\u0bb4\u0bc8")) {
            return WEATHER_ADVISORY;
        }
        if (containsAny(lower, "disease", "rog", "bimari", "pest", "keet",
                "fungus", "wilt", "blight", "yellow", "spots", "rot",
                // Marathi
                "\u0930\u094b\u0917", "\u0915\u093f\u0921", "\u092c\u0941\u0930\u0936\u0940",
                // Telugu
                "\u0c30\u0c4b\u0c17\u0c02", "\u0c2a\u0c41\u0c30\u0c41\u0c17\u0c41",
                // Kannada
                "\u0cb0\u0ccb\u0c97", "\u0c95\u0cc0\u0c9f",
                // Gujarati
                "\u0ab0\u0acb\u0a97", "\u0a9c\u0ac0\u0ab5\u0abe\u0aa4",
                // Punjabi
                "\u0a30\u0a4b\u0a17", "\u0a15\u0a40\u0a1f",
                // Tamil
                "\u0ba8\u0bcb\u0baf\u0bcd", "\u0baa\u0bc2\u0b9a\u0bcd\u0b9a\u0bbf")) {
            return DISEASE_ADVISORY;
        }
        if (containsAny(lower, "scheme", "yojana", "subsidy", "loan", "insurance",
                "government", "sarkar", "msp", "pm-kisan", "fasal bima",
                // Marathi
                "\u092f\u094b\u091c\u0928\u093e", "\u0905\u0928\u0941\u0926\u093e\u0928", "\u0938\u0930\u0915\u093e\u0930",
                // Telugu
                "\u0c2a\u0c25\u0c15\u0c02", "\u0c38\u0c2c\u0c4d\u0c38\u0c3f\u0c21\u0c40", "\u0c2a\u0c4d\u0c30\u0c2d\u0c41\u0c24\u0c4d\u0c35\u0c02",
                // Kannada
                "\u0caf\u0ccb\u0c9c\u0ca8\u0cc6", "\u0cb8\u0cac\u0ccd\u0cb8\u0cbf\u0ca1\u0cbf", "\u0cb8\u0cb0\u0c95\u0cbe\u0cb0",
                // Gujarati
                "\u0aaf\u0acb\u0a9c\u0aa8\u0abe", "\u0ab8\u0aac\u0ab8\u0abf\u0aa1\u0ac0", "\u0ab8\u0ab0\u0a95\u0abe\u0ab0",
                // Punjabi
                "\u0a2f\u0a4b\u0a1c\u0a28\u0a3e", "\u0a38\u0a2c\u0a38\u0a3f\u0a21\u0a40", "\u0a38\u0a30\u0a15\u0a3e\u0a30",
                // Tamil
                "\u0ba4\u0bbf\u0b9f\u0bcd\u0b9f\u0bae\u0bcd", "\u0bae\u0bbe\u0ba9\u0bbf\u0baf\u0bae\u0bcd", "\u0b85\u0bb0\u0b9a\u0bc1")) {
            return SCHEME_ADVISORY;
        }

        return GENERAL_ADVISORY;
    }

    /**
     * Generates an AI-powered response using Gemini for any user query.
     *
     * @param queryText the user's question (in any language)
     * @param intent    detected intent category (for logging only)
     * @param farmerId  optional farmer ID
     * @param language  target language code (hi, en, mr, te, kn)
     * @return AI-generated response text
     */
    public String generateAdvisory(String queryText, String intent, UUID farmerId, String language, java.util.List<VoiceController.ChatMessage> history) {
        log.info("Generating AI response for intent [{}], farmerId [{}], language [{}], query: {}",
                intent, farmerId, language, queryText);

        String lang = language != null ? language.toLowerCase() : "hi";

        // Send everything to Gemini AI
        String response = geminiService.generateResponse(queryText, intent, lang, history);

        if (response != null && !response.isBlank()) {
            log.info("Gemini AI response generated ({} chars)", response.length());
            return response;
        }

        // Fallback if Gemini is completely unavailable
        log.warn("Gemini unavailable, returning fallback message");
        return getFallbackMessage(lang);
    }

    private String getFallbackMessage(String lang) {
        return switch (lang) {
            case "en" -> "I'm sorry, the AI assistant is temporarily unavailable. Please try again in a moment.";
            case "mr" -> "\u092e\u0932\u093e \u092e\u093e\u092b \u0915\u0930\u093e, AI \u0938\u0939\u093e\u092f\u094d\u092f\u0915 \u0938\u0927\u094d\u092f\u093e \u0909\u092a\u0932\u092c\u094d\u0927 \u0928\u093e\u0939\u0940. \u0915\u0943\u092a\u092f\u093e \u0925\u094b\u0921\u094d\u092f\u093e \u0935\u0947\u0933\u093e\u0928\u0947 \u092a\u0941\u0928\u094d\u0939\u093e \u092a\u094d\u0930\u092f\u0924\u094d\u0928 \u0915\u0930\u093e.";
            case "te" -> "\u0c15\u0c4d\u0c37\u0c2e\u0c3f\u0c02\u0c1a\u0c02\u0c21\u0c3f, AI \u0c38\u0c39\u0c3e\u0c2f\u0c15\u0c41\u0c21\u0c41 \u0c24\u0c3e\u0c24\u0c4d\u0c15\u0c3e\u0c32\u0c3f\u0c15\u0c02\u0c17\u0c3e \u0c05\u0c02\u0c26\u0c41\u0c2c\u0c3e\u0c1f\u0c41\u0c32\u0c4b \u0c32\u0c47\u0c21\u0c41. \u0c26\u0c2f\u0c1a\u0c47\u0c38\u0c3f \u0c15\u0c4a\u0c02\u0c24 \u0c38\u0c47\u0c2a\u0c1f\u0c3f \u0c24\u0c30\u0c4d\u0c35\u0c3e\u0c24 \u0c2e\u0c33\u0c4d\u0c33\u0c40 \u0c2a\u0c4d\u0c30\u0c2f\u0c24\u0c4d\u0c28\u0c3f\u0c02\u0c1a\u0c02\u0c21\u0c3f.";
            case "kn" -> "\u0c95\u0ccd\u0cb7\u0cae\u0cbf\u0cb8\u0cbf, AI \u0cb8\u0cb9\u0cbe\u0caf\u0c95 \u0ca4\u0cbe\u0ca4\u0ccd\u0c95\u0cbe\u0cb2\u0cbf\u0c95\u0cb5\u0cbe\u0c97\u0cbf \u0cb2\u0cad\u0ccd\u0caf\u0cb5\u0cbf\u0cb2\u0ccd\u0cb2. \u0ca6\u0caf\u0cb5\u0cbf\u0c9f\u0ccd\u0c9f\u0cc1 \u0cb8\u0ccd\u0cb5\u0cb2\u0ccd\u0caa \u0cb8\u0cae\u0caf\u0ca6 \u0ca8\u0c82\u0ca4\u0cb0 \u0cae\u0ca4\u0ccd\u0ca4\u0cc6 \u0caa\u0ccd\u0cb0\u0caf\u0ca4\u0ccd\u0ca8\u0cbf\u0cb8\u0cbf.";
            case "gu" -> "\u0aae\u0abe\u0aab \u0a95\u0ab0\u0acb, AI \u0ab8\u0ab9\u0abe\u0aaf\u0a95 \u0ab9\u0abe\u0ab2\u0aae\u0abe\u0a82 \u0a89\u0aaa\u0ab2\u0aac\u0acd\u0aa7 \u0aa8\u0aa5\u0ac0. \u0a95\u0ac3\u0aaa\u0abe \u0a95\u0ab0\u0ac0\u0aa8\u0ac7 \u0aa5\u0acb\u0aa1\u0ac0 \u0ab5\u0abe\u0ab0 \u092a\u091b\u0940 \u092b\u0930\u0940 \u092a\u094d\u0930\u092f\u093e\u0938 \u0915\u0930\u094b.";
            case "pa" -> "\u0a2e\u0a41\u0a06\u0a2b \u0a15\u0a30\u0a28\u0a3e, AI \u0a38\u0a39\u0a3e\u0a07\u0a15 \u0a07\u0a38 \u0a35\u0a47\u0a32\u0a47 \u0a09\u0a2a\u0a32\u0a2c\u0a27 \u0a28\u0a39\u0a40\u0a02 \u0a39\u0a48\u0964 \u0a15\u0a3f\u0a30\u0a2a\u0a3e \u0a15\u0a30\u0a15\u0a47 \u0a25\u0a4b\u0a21\u0a3c\u0a40 \u0a26\u0a47\u0a30 \u0a2c\u0a3e\u0a05\u0a26 \u0a26\u0a41\u0a2c\u0a3e\u0a30\u0a3e \u0a15\u0a4b\u0a36\u0a3f\u0a36 \u0a15\u0a30\u0a4b\u0964";
            case "ta" -> "\u0bae\u0ba9\u0bcd\u0ba9\u0bbf\u0b95\u0bcd\u0b95\u0bb5\u0bc1\u0bae\u0bcd, AI \u0b89\u0ba4\u0bb5\u0bbf\u0baf\u0bbe\u0bb3\u0bb0\u0bcd \u0ba4\u0bb1\u0bcd\u0baa\u0bcb\u0ba4\u0bc1 \u0b95\u0bbf\u0b9f\u0bc8\u0b95\u0bcd\u0b95\u0bb5\u0bbf\u0bb2\u0bcd\u0bb2\u0bc8. \u0ba4\u0baf\u0bb5\u0bc1\u0b9a\u0bc6\u0baf\u0bcd\u0ba4\u0bc1 \u0b9a\u0bbf\u0bb1\u0bbf\u0ba4\u0bc1 \u0ba8\u0bc7\u0bb0\u0ba4\u0bcd\u0ba4\u0bbf\u0bb2\u0bcd \u0bae\u0bc0\u0ba3\u0bcd\u0b9f\u0bc1\u0bae\u0bcd \u0bae\u0bc1\u0baf\u0bb1\u0bcd\u0b9a\u0bbf\u0b95\u0bcd\u0b95\u0bb5\u0bc1\u0bae\u0bcd.";
            default -> "\u0915\u094d\u0937\u092e\u093e \u0915\u0930\u0947\u0902, AI \u0938\u0939\u093e\u092f\u0915 \u0905\u092d\u0940 \u0909\u092a\u0932\u092c\u094d\u0927 \u0928\u0939\u0940\u0902 \u0939\u0948\u0964 \u0915\u0943\u092a\u092f\u093e \u0925\u094b\u0921\u093c\u0940 \u0926\u0947\u0930 \u092e\u0947\u0902 \u092a\u0941\u0928\u0903 \u092a\u094d\u0930\u092f\u093e\u0938 \u0915\u0930\u0947\u0902\u0964";
        };
    }

    private boolean containsAny(String text, String... keywords) {
        for (String keyword : keywords) {
            if (text.contains(keyword)) {
                return true;
            }
        }
        return false;
    }
}
