package com.krishimitra.voiceassistant;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.UUID;

/**
 * Detects user intent from transcribed text and generates appropriate advisory responses.
 * Uses keyword-based intent detection optimized for multi-language farm queries.
 * Supports native multi-language responses: hi, en, mr, te, kn.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AdvisoryOrchestrator {

    public static final String CROP_ADVISORY = "CROP_ADVISORY";
    public static final String WEATHER_ADVISORY = "WEATHER_ADVISORY";
    public static final String DISEASE_ADVISORY = "DISEASE_ADVISORY";
    public static final String SCHEME_ADVISORY = "SCHEME_ADVISORY";
    public static final String GENERAL_ADVISORY = "GENERAL_ADVISORY";

    private static final Map<String, String[]> INTENT_KEYWORDS = Map.of(
            CROP_ADVISORY, new String[]{
                    "फसल", "crop", "fasal", "ugana", "बोना", "beej", "बीज",
                    "kheti", "खेती", "girai", "harvest", "बुआई", "pesticide",
                    "कीटनाशक", "orange", "संतरा", "wheat", "गेहूं", "rice", "चावल",
                    "sowing", "fertilizer", "उर्वरक", "seed", "plant", "पौधा"
            },
            WEATHER_ADVISORY, new String[]{
                    "मौसम", "weather", "barish", "बारिश", "तापमान",
                    "temperature", "rain", "hawa", "हवा", "dhoop", "धूप"
            },
            DISEASE_ADVISORY, new String[]{
                    "बीमारी", "disease", "rog", "रोग", "kida", "कीड़ा",
                    "pest", "keeda", "patta", "पत्ता", "fungus", "infection",
                    "blight", "wilt", "spot", "mildew", "rust"
            },
            SCHEME_ADVISORY, new String[]{
                    "योजना", "scheme", "yojana", "सरकारी", "subsidy",
                    "सब्सिडी", "loan", "ऋण", "kisan", "pm-kisan", "insurance",
                    "government", "sarkari"
            }
    );

    /**
     * Detects the user's intent from the transcribed text using keyword matching.
     */
    public String detectIntent(String text) {
        if (text == null || text.isBlank()) {
            return GENERAL_ADVISORY;
        }

        String lowerText = text.toLowerCase().trim();

        for (Map.Entry<String, String[]> entry : INTENT_KEYWORDS.entrySet()) {
            for (String keyword : entry.getValue()) {
                if (lowerText.contains(keyword.toLowerCase())) {
                    log.info("Detected intent [{}] from keyword [{}] in text: {}", entry.getKey(), keyword, text);
                    return entry.getKey();
                }
            }
        }

        log.info("No specific intent detected, defaulting to GENERAL_ADVISORY for text: {}", text);
        return GENERAL_ADVISORY;
    }

    /**
     * Detects specific crops or topics from the text.
     * Returns the topic name (ORANGE, APPLE, POMEGRANATE, GRAPES, BANANA, MANGO, VEGETABLES, SUGARCANE, COTTON, COFFEE_TEA, GROUNDNUT_SOYBEAN, PULSES, HIGH_PROFIT) or null.
     */
    public String detectSpecificTopic(String text) {
        if (text == null || text.isBlank()) {
            return null;
        }
        String lower = text.toLowerCase().trim();

        // 1. Orange
        if (lower.contains("orange") || lower.contains("santra") || lower.contains("संतरा") || lower.contains("संत्रा") || lower.contains("నారింజ") || lower.contains("ಕಿತ್ತಳೆ")) {
            return "ORANGE";
        }
        // 2. Apple
        if (lower.contains("apple") || lower.contains("seeb") || lower.contains("sebi") || lower.contains("sebu") || lower.contains("सेब") || lower.contains("सफरचंद") || lower.contains("ఆపిల్") || lower.contains("ಸೇಬು")) {
            return "APPLE";
        }
        // 3. Pomegranate
        if (lower.contains("pomegranate") || lower.contains("anar") || lower.contains("dalimb") || lower.contains("danimma") || lower.contains("dalimbe") || lower.contains("अनार") || lower.contains("डाळिंब") || lower.contains("ದಾನಿಮ್ಮ") || lower.contains("దానిమ్మ") || lower.contains("ದಾಳಿಂಬೆ")) {
            return "POMEGRANATE";
        }
        // 4. Grapes
        if (lower.contains("grape") || lower.contains("angur") || lower.contains("draksh") || lower.contains("अंगूर") || lower.contains("द्राक्षे") || lower.contains("ద్రాక్ష") || lower.contains("ದ್ರಾಕ್ಷಿ")) {
            return "GRAPES";
        }
        // 5. Banana
        if (lower.contains("banana") || lower.contains("kela") || lower.contains("keli") || lower.contains("केला") || lower.contains("केळी") || lower.contains("అరటి") || lower.contains("ಬಾಳೆಹಣ್ಣು")) {
            return "BANANA";
        }
        // 6. Mango
        if (lower.contains("mango") || lower.contains("aam") || lower.contains("amba") || lower.contains("mamidi") || lower.contains("mavu") || lower.contains("आम") || lower.contains("आंबा") || lower.contains("మామిడి") || lower.contains("ಮಾವು")) {
            return "MANGO";
        }
        // 7. Sugarcane
        if (lower.contains("sugarcane") || lower.contains("ganna") || lower.contains("us") || lower.contains("cheraku") || lower.contains("kabbu") || lower.contains("गन्ना") || lower.contains("ऊस") || lower.contains("చెరకు") || lower.contains("ಕಬ್ಬು")) {
            return "SUGARCANE";
        }
        // 8. Cotton
        if (lower.contains("cotton") || lower.contains("kapas") || lower.contains("kapus") || lower.contains("patti") || lower.contains("hatti") || lower.contains("कपास") || lower.contains("कापूस") || lower.contains("పత్తి") || lower.contains("ಹತ್ತಿ")) {
            return "COTTON";
        }
        // 9. Coffee, Tea, Spices
        if (lower.contains("coffee") || lower.contains("tea") || lower.contains("chaha") || lower.contains("kafi") || lower.contains("ginger") || lower.contains("garlic") || lower.contains("cardamom") || lower.contains("लहसुन") || lower.contains("अदरक") || lower.contains("मसाले") || lower.contains("कॉफ़ी") || lower.contains("चहा") || lower.contains("కాఫీ") || lower.contains("ಕಾಫಿ") || lower.contains("ಚಹಾ")) {
            return "COFFEE_TEA";
        }
        // 10. Oilseeds (Groundnut, Mustard, Soybean)
        if (lower.contains("groundnut") || lower.contains("mustard") || lower.contains("soybean") || lower.contains("soya") || lower.contains("mungfali") || lower.contains("bhuimug") || lower.contains("verusanaga") || lower.contains("kadalekayi") || lower.contains("sarso") || lower.contains("मूंगफली") || lower.contains("भुईमूग") || lower.contains("वेరుశనగ") || lower.contains("ಕಡಲೆಕಾಯಿ") || lower.contains("सोयाबीन") || lower.contains("सरसों") || lower.contains("मोहरी")) {
            return "GROUNDNUT_SOYBEAN";
        }
        // 11. Pulses (Pulses, Gram, Chickpea, Lentil, Pigeon Pea, Mung, Dal)
        if (lower.contains("pulse") || lower.contains("pulses") || lower.contains("dal") || lower.contains("dali") || lower.contains("chana") || lower.contains("pigeonpea") || lower.contains("lentil") || lower.contains("चना") || lower.contains("अरहर") || lower.contains("मसूर") || lower.contains("मूंग") || lower.contains("उड़द") || lower.contains("डाळी") || lower.contains("పప్పులు") || lower.contains("ಬೇಳೆಕಾಳುಗಳು") || lower.contains("दाल") || lower.contains("तूर") || lower.contains("हरभरा")) {
            return "PULSES";
        }
        // 12. Vegetables
        if (lower.contains("vegetable") || lower.contains("sabji") || lower.contains("bhaji") || lower.contains("kuragaya") || lower.contains("tarakari") || lower.contains("सब्जी") || lower.contains("सब्जियां") || lower.contains("भाजी") || lower.contains("भाज्या") || lower.contains("కూरగాయలు") || lower.contains("ತರಕಾರಿ") ||
            lower.contains("tomato") || lower.contains("onion") || lower.contains("potato") || lower.contains("टमाटर") || lower.contains("प्याज") || lower.contains("आलू") || lower.contains("टोमॅटो") || lower.contains("कांदा") || lower.contains("बटाटा") || lower.contains("టమోటా") || lower.contains("ఉల్లిపాయ") || lower.contains("బంగాళాదుంప") || lower.contains("ಟೊಮೆಟೊ") || lower.contains("ಈರುಳ್ಳಿ") || lower.contains("ಆಲೂಗಡ್ಡೆ")) {
            return "VEGETABLES";
        }
        // 13. High Profit
        if (lower.contains("profit") || lower.contains("income") || lower.contains("cash crop") || lower.contains("earning") || lower.contains("munafa") || lower.contains("fayda") || lower.contains("nafa") || lower.contains("labha") || lower.contains("मुनाफा") || lower.contains("फायदा") || lower.contains("नफा") || lower.contains("उत्पन्न") || lower.contains("లాభం") || lower.contains("లాభదాయక") || lower.contains("ಲಾಭ") || lower.contains("ಲಾಭದಾಯಕ") || lower.contains("नगदी") || lower.contains("कमाई")) {
            return "HIGH_PROFIT";
        }

        return null;
    }

    /**
     * Generates an advisory response based on the detected intent in the requested language.
     */
    public String generateAdvisory(String queryText, String intent, UUID farmerId) {
        return generateAdvisory(queryText, intent, farmerId, "hi");
    }

    /**
     * Generates an advisory response based on the detected intent in the specified language.
     * Intercepts and answers specific crop and farming queries natively.
     *
     * @param queryText the user's query
     * @param intent    the detected intent
     * @param farmerId  optional farmer ID
     * @param language  target language code (hi, en, mr, te, kn)
     * @return advisory text in the requested language
     */
    public String generateAdvisory(String queryText, String intent, UUID farmerId, String language) {
        log.info("Generating advisory for intent [{}], farmerId [{}], language [{}], query: {}",
                intent, farmerId, language, queryText);

        String lang = language != null ? language.toLowerCase() : "hi";

        // Check for specific agricultural topics first
        String specificTopic = detectSpecificTopic(queryText);
        if (specificTopic != null) {
            log.info("Routing query to specific topic advisory: {}", specificTopic);
            return getSpecificAdvisory(specificTopic, lang);
        }

        // Fall back to category-based intent templates
        return switch (intent) {
            case CROP_ADVISORY -> getCropAdvisory(lang);
            case WEATHER_ADVISORY -> getWeatherAdvisory(lang);
            case DISEASE_ADVISORY -> getDiseaseAdvisory(lang);
            case SCHEME_ADVISORY -> getSchemeAdvisory(lang);
            default -> getGeneralAdvisory(lang);
        };
    }

    /**
     * Specific multi-language advisory routing.
     */
    private String getSpecificAdvisory(String topic, String lang) {
        return switch (topic) {
            case "ORANGE" -> getOrangeAdvisory(lang);
            case "APPLE" -> getAppleAdvisory(lang);
            case "POMEGRANATE" -> getPomegranateAdvisory(lang);
            case "GRAPES" -> getGrapesAdvisory(lang);
            case "BANANA" -> getBananaAdvisory(lang);
            case "MANGO" -> getMangoAdvisory(lang);
            case "SUGARCANE" -> getSugarcaneAdvisory(lang);
            case "COTTON" -> getCottonAdvisory(lang);
            case "COFFEE_TEA" -> getCoffeeTeaAdvisory(lang);
            case "GROUNDNUT_SOYBEAN" -> getGroundnutSoybeanAdvisory(lang);
            case "PULSES" -> getPulsesAdvisory(lang);
            case "VEGETABLES" -> getVegetablesAdvisory(lang);
            case "HIGH_PROFIT" -> getHighProfitAdvisory(lang);
            default -> getGeneralAdvisory(lang);
        };
    }

    // ── Orange Advisory ────────────────────────────────────────────────────────

    private String getOrangeAdvisory(String lang) {
        return switch (lang) {
            case "en" -> """
                    Hello farmer! Here is specific advice on Orange (Citrus) cultivation:
                    
                    1. **Soil & Climate**: Requires well-drained sandy loam soil with pH 6.0–7.5. Grows best in subtropical climates.
                    2. **Irrigation**: Highly sensitive to water logging. Drip irrigation is highly recommended to manage watering.
                    3. **Nutrients**: Requires high nitrogen and potassium. Apply organic manure (FYM) and micro-nutrients (Zinc, Iron) annually.
                    4. **Disease**: Citrus Canker is common. Spray Copper Oxychloride (2.5g/L) to prevent leaf/fruit spots.
                    5. **Profitability**: Orange orchards start fruiting in 4-5 years and remain profitable for over 20 years. Intercropping with vegetables in early years yields extra income.""";
            case "mr" -> """
                    नमस्कार शेतकरी बंधू! संत्रा लागवडीसंबंधी सविस्तर माहिती:
                    
                    1. **जमीन व हवामान**: संत्रा पिकासाठी मध्यम ते भारी, उत्तम निचऱ्याची जमीन (pH ६.० ते ७.५) आवश्यक असते.
                    2. **पाणी व्यवस्थापन**: झाडाच्या मुळाशी पाणी साचू देऊ नका. ठिबक सिंचनाचा वापर करणे अत्यंत फायदेशीर ठरते.
                    3. **खत व्यवस्थापन**: वर्षातून एकदा शेणखतासोबत नत्र, स्फुरद आणि पालाश द्या. जस्त आणि लोहाची कमतरता टाळण्यासाठी फवारणी करा.
                    4. **रोग नियंत्रण**: तांबेयुक्त बुरशीनाशकाची (कॉपर ऑक्सीक्लोराईड २.५ ग्रॅम/लिटर) फवारणी सिट्रस कॅंकर रोगाचा अटकाव करते.
                    5. **नफा**: लागवडीनंतर ४-५ वर्षांनी फळे मिळण्यास सुरुवात होते. पहिल्या ३-४ वर्षांत भाजीपाला किंवा आंतरपिके घेऊन अधिक नफा कमवता येतो.""";
            case "te" -> """
                    నమస్కారం రైతు సోదరా! నారింజ (బత్తాయి) సాగుకు ప్రత్యేక సూచనలు:
                    
                    1. **నేల మరియు వాతావరణం**: నీరు నిల్వ ఉండని ఇసుక లోమ్ నేలలు, pH 6.0-7.5 అనుకూలం. సమశీతోష్ణ వాతావరణం అవసరం.
                    2. **నీటి యాజమాన్యం**: వేర్ల వద్ద నీరు నిల్వ ఉంటే ఉడికిపోతాయి, కాబట్టి డ్రిప్ సిస్టమ్ వాడండి.
                    3. **ఎరువులు**: పశువుల ఎరువుతో పాటు నత్రజని, పొటాషియం అందించండి. జింక్, ఇనుము వంటి సూక్ష్మపోషకాలు చల్లండి.
                    4. **తెగుళ్లు**: సిట్రస్ క్యాంకర్ తెగులు నివారణకు కాపర్ ఆక్సిక్లోరైడ్ (2.5 గ్రా/లీటర్) పిచికారీ చేయండి.
                    5. **లాభాలు**: 4-5 సంవత్సరాల తర్వాత దిగుబడి ప్రారంభమై 20 ఏళ్ల పాటు నిరంతర ఆదాయం మరియు లాభాలు ఇస్తుంది.""";
            case "kn" -> """
                    ನಮಸ್ಕಾರ ರೈತ ಬಂಧು! ಕಿತ್ತಳೆ ಬೇಸಾಯದ ಬಗ್ಗೆ ನಿಖರ ಮಾಹಿತಿ:
                    
                    1. **ಮಣ್ಣು ಮತ್ತು ಹವಾಮಾನ**: ಉತ್ತಮ ಒಳಚರಂಡಿ ಹೊಂದಿರುವ ಮರಳು ಮಿಶ್ರಿತ ಲೋಮ್ ಮಣ್ಣು ಮತ್ತು pH 6.0-7.5 ಸೂಕ್ತ.
                    2. **ನೀರಾವರಿ**: ಬೇರುಗಳಲ್ಲಿ ನೀರು ನಿಲ್ಲದಂತೆ ನೋಡಿಕೊಳ್ಳಲು ಹನಿ ನೀರಾವರಿ ಪದ್ಧತಿ ಅಳವಡಿಸಿ.
                    3. **ಗೊಬ್ಬರ**: ಕೊಟ್ಟಿಗೆ ಗೊಬ್ಬರದೊಂದಿಗೆ ಸಾರಜನಕ ಮತ್ತು ಪೊಟ್ಯಾಷಿಯಂ ಒದಗಿಸಿ. ಸತು ಮತ್ತು ಕಬ್ಬಿಣದ ಸಿಂಪರಣೆ ಮಾಡಿ.
                    4. **ರೋಗ ತಡೆ**: ಕಿತ್ತಳೆಯಲ್ಲಿ ಬರುವ ಸಿಟ್ರಸ್ ಕ್ಯಾಂಕರ್ ರೋಗ ತಡೆಗೆ ತಾಮ್ರದ ಆಕ್ಸಿಕ್ಲೋರೈಡ್ (2.5 ಗ್ರಾಂ/ಲೀಟರ್) ಸಿಂಪಡಿಸಿ.
                    5. **ಲಾಭ**: ನಾಟಿ ಮಾಡಿದ 4-5 ವರ್ಷಗಳ ನಂತರ ಗಿಡಗಳು ಫಲ ನೀಡುತ್ತವೆ. ಆರಂಭಿಕ ವರ್ಷಗಳಲ್ಲಿ ತರಕಾರಿ ಬೆಳೆದು ಉಪ-ಆದಾಯ ಪಡೆಯಬಹುದು.""";
            default -> """
                    नमस्ते किसान भाई! संतरे (Citrus) की खेती के लिए विशिष्ट सलाह:
                    
                    1. **मिट्टी और जलवायु**: उपोष्णकटिबंधीय जलवायु और 6.0-7.5 pH वाली अच्छे जल निकासी वाली बलुई दोमट मिट्टी सबसे उपयुक्त है।
                    2. **सिंचाई**: यह जलभराव के प्रति संवेदनशील है। सिंचाई प्रबंधन के लिए ड्रिप (टपक) विधि का उपयोग करें।
                    3. **खाद**: गोबर की खाद के साथ नाइट्रोजन और पोटैशियम प्रचुर मात्रा में दें। जिंक और आयरन का छिड़काव करें।
                    4. **रोग**: सिट्रस कैंकर से बचाव के लिए कॉपर ऑक्सीक्लोराइड (2.5 ग्राम/लीटर) का छिड़काव करें।
                    5. **मुनाफा**: 4-5 वर्षों में फल आना शुरू होते हैं जो 20 वर्षों तक चलते हैं। शुरुआती वर्षों में सब्जियों की अंतरफसली खेती से अतिरिक्त लाभ कमाएं।""";
        };
    }

    // ── Apple Advisory ─────────────────────────────────────────────────────────

    private String getAppleAdvisory(String lang) {
        return switch (lang) {
            case "en" -> """
                    Hello farmer! Here is specific advice on Apple cultivation:
                    
                    1. **Climate & Soil**: Requires temperate climate with 1000-1500 chilling hours below 7°C. Prefers deep, well-drained loamy soils (pH 5.5-6.5).
                    2. **Pruning**: Regular pruning in winter is critical to maintain sunlight access and fruit quality.
                    3. **Fertilization**: Apply well-rotted manure, Nitrogen, Phosphorus, and Potassium in late winter.
                    4. **Pests**: Apple Scab and Codling Moth are major issues. Use fungicide sprays before blossom.
                    5. **Profitability**: Extremely high-value crop in hilly/high-altitude zones. Ultra-High-Density planting yields early returns.""";
            case "mr" -> """
                    नमस्कार शेतकरी बंधू! सफरचंद लागवडीसंबंधी माहिती:
                    
                    1. **हवामान व जमीन**: सफरचंदाला थंड हवामान व भरपूर थंडी आवश्यक असते. खोल, चांगला निचरा असणारी सुपीक पोयट्याची जमीन (pH ५.५ ते ६.५) लागते.
                    2. **छाटणी (Pruning)**: हिवाळ्यात झाडांची योग्य छाटणी केल्यास फळांची गुणवत्ता वाढते आणि सूर्यप्रकाश मिळतो.
                    3. **खते**: हिवाळ्याच्या शेवटी भरपूर शेणखत आणि NPK खतांचा वापर करा.
                    4. **कीड नियंत्रण**: सफरचंदावरील खवले रोग (Scab) रोखण्यासाठी बुरशीनाशकांची वेळेवर फवारणी करा.
                    5. **नफा**: डोंगराळ व थंड भागातील हा सर्वात फायदेशीर व्यवसाय आहे. सघन लागवड केल्यास कमी जागेत जास्त उत्पन्न मिळते.""";
            case "te" -> """
                    నమస్కారం రైతు సోదరా! ఆపిల్ సాగుకు ప్రత్యేక సూచనలు:
                    
                    1. **వాతావరణం మరియు నేల**: చల్లని వాతావరణం, లోతైన మరియు సారవంతమైన లోమ్ నేలలు (pH 5.5-6.5) ఆపిల్ సాగుకు అవసరం.
                    2. **కత్తిరింపు (Pruning)**: శీతాకాలంలో రెమ్మలను సరిగ్గా కత్తిరించడం వల్ల పూత మరియు నాణ్యత పెరుగుతాయి.
                    3. **ఎరువులు**: చలికాలం చివరలో సేంద్రీయ ఎరువులతో పాటు సమతుల్య NPK ఎరువులు వేయండి.
                    4. **తెగుళ్లు**: ఆపిల్ స్కాబ్ తెగులు రాకుండా మొగ్గ దశకు ముందే నివారణ మందులు చల్లండి.
                    5. **లాభాలు**: చల్లటి పర్వత ప్రాంతాలలో ఇది బంగారు పంట. హై-డెన్సిటీ నాటడం పద్ధతి ద్వారా త్వరగా మరియు ఎక్కువ లాభాలు పొందవచ్చు.""";
            case "kn" -> """
                    ನಮಸ್ಕಾರ ರೈತ ಬಂಧು! ಸೇಬು ಬೇಸಾಯದ ಬಗ್ಗೆ ಪ್ರಮುಖ ಸಲಹೆಗಳು:
                    
                    1. **ಹವಾಮಾನ ಮತ್ತು ಮಣ್ಣು**: ತಂಪಾದ ಹವಾಮಾನ ಹಾಗೂ ಆಳವಾದ, ಫಲವತ್ತಾದ ಕೆಂಪು ಮರಳು ಮಿಶ್ರಿತ ಮಣ್ಣು (pH 5.5-6.5) ಅಗತ್ಯ.
                    2. **ಕತ್ತರಿಕೆ (Pruning)**: ಚಳಿಗಾಲದಲ್ಲಿ ಕೊಂಬೆಗಳನ್ನು ಕತ್ತರಿಸುವುದರಿಂದ ಹಣ್ಣುಗಳ ಗಾತ್ರ ಮತ್ತು ಗುಣಮಟ್ಟ ಉತ್ತಮವಾಗುತ್ತದೆ.
                    3. **ಗೊಬ್ಬರ**: ಚಳಿಗಾಲದ ಕೊನೆಯಲ್ಲಿ ಮಣ್ಣಿಗೆ ಸಾಕಷ್ಟು ಕೊಟ್ಟಿಗೆ ಗೊಬ್ಬರ ಮತ್ತು NPK ರಾಸಾಯನಿಕಗಳನ್ನು ಒದಗಿಸಿ.
                    4. **ರೋಗಗಳು**: ಸೇಬಿನಲ್ಲಿ ಕಂಡುಬರುವ ಸ್ಕ್ಯಾಬ್ ರೋಗದ ತಡೆಗೆ ಹೂ ಬಿಡುವ ಮುನ್ನ ಬೂಷ್ಟುನಾಶಕ ಸಿಂಪಡಿಸಿ.
                    5. **ಲಾಭ**: ತಂಪು ಗುಡ್ಡಗಾಡು ಪ್ರದೇಶದ ರೈತರಿಗೆ ಇದು ಅತ್ಯಧಿಕ ಆದಾಯ ನೀಡುವ ಹಣ್ಣಿನ ಬೆಳೆಯಾಗಿದೆ.""";
            default -> """
                    नमस्ते किसान भाई! सेब की खेती के लिए विशिष्ट सलाह:
                    
                    1. **जलवायु और मिट्टी**: सेब के लिए ठंडी जलवायु (7°C से कम तापमान वाले 1000 घंटे) और गहरी, दोमट मिट्टी (pH 5.5-6.5) सर्वोत्तम है।
                    2. **कटाई-छंटाई**: सर्दियों में पेड़ों की कटाई-छंटाई अवश्य करें ताकि सूर्य का प्रकाश अंदर तक पहुंच सके।
                    3. **उर्वरक**: सर्दियों के अंत में अच्छी तरह सड़ी हुई गोबर की खाद और एनपीके (NPK) डालें।
                    4. **कीट और रोग**: सेब के स्कैब (Scab) और कीटों से बचाव के लिए फूल आने से पहले फफूंदनाशक का छिड़काव करें।
                    5. **मुनाफा**: पहाड़ी क्षेत्रों में यह सोने की खान है। सघन बागवानी (Ultra-High-Density) तकनीक से कम समय में भारी मुनाफा मिलता है।""";
        };
    }

    // ── Pomegranate Advisory ───────────────────────────────────────────────────

    private String getPomegranateAdvisory(String lang) {
        return switch (lang) {
            case "en" -> """
                    Hello farmer! Here is specific advice on Pomegranate cultivation:
                    
                    1. **Soil & Climate**: Thrives in dry, hot summer climates. Prefers deep, well-drained sandy loam soil.
                    2. **Irrigation**: Drought tolerant but regular drip irrigation is required for high fruit yield and to prevent fruit cracking.
                    3. **Bacterial Blight**: Major threat. Spray Streptocycline (0.5g/L) combined with Copper Oxychloride.
                    4. **Training**: Train plants into multi-stem bushes (3-4 stems) for easier disease control.
                    5. **Profitability**: Very high profit cash crop. Yield begins in 2-3 years, with peak yields between 5-10 years.""";
            case "mr" -> """
                    नमस्कार शेतकरी बंधू! डाळिंब लागवडीसंबंधी सविस्तर सल्ला:
                    
                    1. **हवामान व जमीन**: डाळिंबाला कोरडे व उष्ण हवामान चांगले मानवते. निचरा होणारी मध्यम ते हलकी जमीन निवडावी.
                    2. **पाणी व्यवस्थापन**: डाळिंब फळे तडकणे (Fruit Cracking) टाळण्यासाठी उन्हाळ्यात नियमित व समप्रमाणात ठिबक सिंचनाने पाणी द्या.
                    3. **तेल्या रोग नियंत्रण**: तेल्या रोगाचा प्रादुर्भाव रोखण्यासाठी स्ट्रेप्टोसायक्लीन (०.५ ग्रॅम) अधिक कॉपर ऑक्सीक्लोराईडची एकत्र फवारणी करा.
                    4. **आकार देणे (Pruning)**: झाडाला ३ ते ४ मुख्य खोड ठेवून छत्रीसारखा आकार द्यावा.
                    5. **नफा**: अतिशय फायदेशीर कोरडवाहू फळपीक आहे. लागवडीनंतर तिसऱ्या वर्षापासून दर्जेदार उत्पादन सुरू होऊन २५-३० वर्षांपर्यंत उत्पन्न मिळते.""";
            case "te" -> """
                    నమస్కారం రైతు సోదరా! దానిమ్మ సాగుకు ప్రత్యేక సూచనలు:
                    
                    1. **నేల మరియు వాతావరణం**: పొడి మరియు వేడి వాతావరణం, లోతైన ఇసుక నేలలు దానిమ్మకు అనుకూలం.
                    2. **నీటి యాజమాన్యం**: కాయలు పగలకుండా (Fruit cracking) ఉండటానికి క్రమ పద్ధతిలో డ్రిప్ ద్వారా నీటిని అందించాలి.
                    3. **బాక్టీరియల్ బ్లైట్**: దీని నివారణకు స్ట్రెప్టోసైక్లిన్ (0.5 గ్రా/లీ) తో పాటు కాపర్ ఆక్సిక్లోరైడ్ పిచికారీ చేయండి.
                    4. **కత్తిరింపు**: గాలి, వెలుతురు బాగా తగిలేలా బహుళ-కాండం (Multi-stem) పద్ధతిలో కత్తిరించుకోవాలి.
                    5. **లాభాలు**: తక్కువ నీటితో పండే అత్యధిక ఆదాయం ఇచ్చే తోట పంట. 2-3 సంవత్సరాల్లో కాపుకు వచ్చి ఎకరాకు భారీ లాభాలు ఇస్తుంది.""";
            case "kn" -> """
                    ನಮಸ್ಕಾರ ರೈತ ಬಂಧು! ದಾಳಿಂಬೆ ಬೇಸಾಯದ ಬಗ್ಗೆ ನಿಖರ ಮಾಹಿತಿ:
                    
                    1. **ಮಣ್ಣು ಮತ್ತು ಹವಾಮಾನ**: ಒಣ ಮತ್ತು ಬಿಸಿ ಹವಾಮಾನ ಸೂಕ್ತ. ಉತ್ತಮ ಒಳಚರಂಡಿ ಇರುವ ಮರಳು ಮಿಶ್ರಿತ ಕೆಂಪು ಮಣ್ಣು ಆಯ್ದುಕೊಳ್ಳಿ.
                    2. **ನೀರಾವರಿ**: ಹಣ್ಣು ಬಿರುಕು ಬಿಡುವುದನ್ನು (Fruit Cracking) ತಡೆಯಲು ನಿಯಮಿತವಾಗಿ ಹನಿ ನೀರಾವರಿ ಒದಗಿಸಿ.
                    3. **ತೆಲೆಯಾ ರೋಗ (Bacterial Blight)**: ದಾಳಿಂಬೆಗೆ ಮಾರಕವಾಗಿರುವ ಈ ರೋಗ ತಡೆಗೆ ಸ್ಟ್ರೆಪ್ಟೋಸೈಕ್ಲಿನ್ (0.5 ಗ್ರಾಂ/ಲೀ) ಮತ್ತು ತಾಮ್ರದ ಆಕ್ಸಿಕ್ಲೋರೈಡ್ ಬಳಸಿ.
                    4. **ತರಬೇತಿ**: ಗಿಡವನ್ನು ಪೊದೆಯಂತೆ ಬೆಳೆಸಲು ಸೂಕ್ತ ಕತ್ತರಿಕೆ ವಿಧಾನ ಅನುಸರಿಸಿ.
                    5. **ಲಾಭ**: ಇದು ಒಣಭೂಮಿ ಬೇಸಾಯದ ಅತ್ಯಂತ ಲಾಭದಾಯಕ ಬೆಳೆಯಾಗಿದ್ದು, 2-3 ವರ್ಷಗಳಲ್ಲಿ ಗಿಡಗಳು ಬೆಳೆ ನೀಡಲಾರಂಭಿಸುತ್ತವೆ.""";
            default -> """
                    नमस्ते किसान भाई! अनार की खेती के लिए विशिष्ट सलाह:
                    
                    1. **मिट्टी और जलवायु**: अनार गर्म और शुष्क जलवायु में सबसे अच्छा फलता-फूलता है। गहरी, दोमट और जल निकास वाली मिट्टी चुनें।
                    2. **सिंचाई**: अनार सूखा सहन कर सकता है, लेकिन अच्छी उपज और फलों को फटने (Cracking) से बचाने के लिए ड्रिप सिंचाई अवश्य करें।
                    3. **तेल्या रोग (Bacterial Blight)**: इसके नियंत्रण के लिए स्ट्रेप्टोसाइक्लिन (0.5 ग्राम/लीटर) के साथ कॉपर ऑक्सीक्लोराइड का छिड़काव करें।
                    4. **कटाई**: बेहतर हवा और धूप के लिए झाड़ीदार बहु-तना (Multi-stem) प्रणाली अपनाएं।
                    5. **मुनाफा**: यह एक उच्च मुनाफे वाली फसल है। 2-3 साल में फसल मिलने लगती है और प्रति एकड़ लाखों की शुद्ध कमाई होती है।""";
        };
    }

    // ── Grapes Advisory ────────────────────────────────────────────────────────

    private String getGrapesAdvisory(String lang) {
        return switch (lang) {
            case "en" -> """
                    Hello farmer! Here is specific advice on Grapes cultivation:
                    
                    1. **Soil & Support**: Needs well-drained loamy soil (pH 6.5-7.5). Requires Bower or Trellis training systems.
                    2. **Pruning**: Crucial step. Pruning is done twice a year (April for growth/back pruning, October for fruiting/forward pruning).
                    3. **Water**: High water requirement during growth, but dry weather during ripening. Drip irrigation is essential.
                    4. **Disease**: Powdery Mildew and Downy Mildew are severe. Spray Sulfur or Systemic fungicides during early shoots.
                    5. **Profitability**: Highly profitable for table grapes and raisin production. Drip-fertigation saves 30% fertilizer costs.""";
            case "mr" -> """
                    नमस्कार शेतकरी बंधू! द्राक्ष बागेसंबंधी सविस्तर सल्ला:
                    
                    1. **आधार व जमीन**: द्राक्षासाठी भुसभुशीत, सुपीक व निचऱ्याची जमीन लागते. मांडव किंवा टेलिफोन पद्धतीचा आधार देणे आवश्यक आहे.
                    2. **छाटणी (Pruning)**: वर्षातून दोन छाटण्या आवश्यक असतात (एप्रिलमध्ये खरड छाटणी व ऑक्टोबरमध्ये गोड छाटणी).
                    3. **पाणी व्यवस्थापन**: वेल वाढताना भरपूर पाणी द्या, परंतु फळे पिकताना कोरडे हवामान आवश्यक आहे. ठिबक सिंचन वापरा.
                    4. **रोग नियंत्रण**: भुरी (Powdery Mildew) आणि केवडा (Downy Mildew) नियंत्रणासाठी बोर्डो मिश्रण किंवा सल्फरची योग्य वेळी फवारणी करा.
                    5. **नफा**: द्राक्ष लागवड ही देशातील सर्वाधिक नफा देणारी शेती आहे. मनुके किंवा बेदाणे बनवून अधिक उत्पन्न मिळवता येते.""";
            case "te" -> """
                    నమస్కారం రైతు సోదరా! ద్రాక్ష సాగుకు ప్రత్యేక సూచనలు:
                    
                    1. **మద్దతు మరియు నేల**: మంచి సారవంతమైన నేలలు అనుకూలం. ద్రాక్ష తీగ పాకడానికి పందిరి లేదా ట్రైలిస్ పద్ధతి అవసరం.
                    2. **కత్తిరింపు**: సంవత్సరానికి రెండుసార్లు కత్తిరింపు చేయాలి (ఏప్రిల్ లో కొమ్మల అభివృద్ధికి, అక్టోబర్ లో పూత కొరకు).
                    3. **నీరు**: కాయలు పక్వానికి వచ్చేటప్పుడు వాతావరణం పొడిగా ఉండాలి. డ్రిప్ పద్ధతి ద్వారా సమతులంగా నీరు ఇవ్వాలి.
                    4. **తెగుళ్లు**: బూడిద తెగులు, ఆకుమచ్చ తెగులు నివారణకు సల్ఫర్ లేదా తగిన శిలీంద్ర నాశక మందులు వాడండి.
                    5. **లాభాలు**: టేబుల్ ద్రాక్ష మరియు కిస్మిస్ తయారీ ద్వారా ఎకరాకు చాలా ఎక్కువ లాభాలు పొందవచ్చు.""";
            case "kn" -> """
                    ನಮಸ್ಕಾರ ರೈತ ಬಂಧು! ದ್ರಾಕ್ಷಿ ಬೆಳೆಯ ಬಗ್ಗೆ ಪ್ರಮುಖ ಮಾಹಿತಿ:
                    
                    1. **ಆಧಾರ ಮತ್ತು ಮಣ್ಣು**: ಫಲವತ್ತಾದ ಮಣ್ಣು (pH 6.5-7.5) ಹಾಗೂ ಬಳ್ಳಿಗಳು ಹರಡಲು ಪಂದಲ್ ಅಥವಾ ಟ್ರೇಲಿಸ್ ವಿಧಾನದ ಆಧಾರ ಕಡ್ಡಾಯ.
                    2. **ಕತ್ತರಿಕೆ**: ವರ್ಷದಲ್ಲಿ ಎರಡು ಬಾರಿ ಕೊಂಬೆ ಕತ್ತರಿಸಬೇಕು (ಏಪ್ರಿಲ್‌ನಲ್ಲಿ ರಸಕತ್ತರಿಕೆ ಮತ್ತು ಅಕ್ಟೋಬರ್‌ನಲ್ಲಿ ಫಲಕತ್ತರಿಕೆ).
                    3. **ನೀರಾವರಿ**: ದ್ರಾಕ್ಷಿ ಹಣ್ಣಾಗುವಾಗ ಒಣ ಹವಾಮಾನ ಬೇಕು. ಹನಿ ನೀರಾವರಿ ವಿಧಾನದಿಂದ ನೀರು ಒದಗಿಸಿ.
                    4. **ರೋಗ ತಡೆ**: ಬೂದಿ ರೋಗ ಮತ್ತು ಮೃದು ರೋಗ ತಡೆಗೆ ಗಂಧಕದ ಪುಡಿ ಅಥವಾ ಸೂಕ್ತ ಬೂಷ್ಟುನಾಶಕಗಳನ್ನು ಬಳಸಿ.
                    5. **ಲಾಭ**: ಒಣ ದ್ರಾಕ್ಷಿ (ದ್ರಾಕ್ಷಿ ಹಣ್ಣುಗಳನ್ನು ಒಣಗಿಸಿ ಒಣದ್ರಾಕ್ಷಿ ಮಾಡುವುದು) ತಯಾರಿಕೆಯಿಂದ ಭಾರಿ ಪ್ರಮಾಣದ ಲಾಭ ಗಳಿಸಬಹುದು.""";
            default -> """
                    नमस्ते किसान भाई! अंगूर की खेती के लिए विशिष्ट सलाह:
                    
                    1. **मिट्टी और सहारा**: अच्छी जल निकासी वाली दोमट मिट्टी (pH 6.5-7.5) चुनें। लताओं को सहारा देने के लिए पंडाल या ट्रेलिस (Trellis) प्रणाली आवश्यक है।
                    2. **कटाई-छंटाई**: साल में दो बार कटाई-छंटाई करें (अप्रैल में विकास के लिए, अक्टूबर में फल आने के लिए)।
                    3. **सिंचाई**: फल पकते समय मौसम शुष्क होना चाहिए। ड्रिप सिंचाई का उपयोग करें।
                    4. **रोग**: डाउनी मिल्ड्यू और पाउडर फफूंद से बचाव के लिए सल्फर युक्त या प्रणालीगत फफूंदनाशक का छिड़काव करें।
                    5. **मुनाफा**: टेबल अंगूर औरकश्मीश उत्पादन से बहुत अधिक मुनाफा कमाया जा सकता है। ड्रिप-फर्टिगेशन से लागत कम होती है।""";
        };
    }

    // ── Banana Advisory ────────────────────────────────────────────────────────

    private String getBananaAdvisory(String lang) {
        return switch (lang) {
            case "en" -> """
                    Hello farmer! Here is specific advice on Banana cultivation:
                    
                    1. **Planting**: Use healthy Tissue Culture plants for uniform growth and disease-free crops. Plant at a spacing of 1.8m x 1.8m.
                    2. **Nutrients**: Banana is a heavy feeder. Apply high nitrogen, phosphorus, and potassium, along with organic compost.
                    3. **Water**: Requires constant moisture. Use drip irrigation to ensure wet soil without water logging.
                    4. **Pest/Disease**: Sigatoka Leaf Spot is a major threat. Spray Propiconazole (1 ml/L) to manage it.
                    5. **Profitability**: Yields in 11-12 months. Quick cash flow makes it a popular profitable commercial fruit.""";
            case "mr" -> """
                    नमस्कार शेतकरी बंधू! केळी लागवडीसंबंधी सविस्तर सल्ला:
                    
                    1. **लागवड**: रोगांपासून मुक्त राहण्यासाठी व एकसमान वाढीसाठी उती संवर्धन (Tissue Culture) रोपांचा वापर करा. लागवड १.८ x १.८ मीटर अंतरावर करा.
                    2. **खत व्यवस्थापन**: केळी हे जास्त अन्नद्रव्ये लागणारे पीक आहे. भरपूर सेंद्रिय खतांसोबत नत्र आणि पालाश खतांची नियमित मात्रा द्या.
                    3. **पाणी**: जमिनीत सतत ओल असावी. दलदल न करता नियमित पाणी देण्यासाठी ठिबक सिंचनाचा वापर करा.
                    4. **रोग नियंत्रण**: केळीवरील करपा (Sigatoka Leaf Spot) रोगासाठी प्रोपिकोनाझोल (१ मिली प्रति लिटर पाणी) फवारा.
                    5. **नफा**: अवघ्या ११-१२ महिन्यांत उत्पन्न मिळण्यास सुरुवात होते. जलद रोख पैसा देणारे केळी हे एक उत्कृष्ट व्यापारी पीक आहे.""";
            case "te" -> """
                    నమస్కారం రైతు సోదరా! అరటి సాగుకు ప్రత్యేక సూచనలు:
                    
                    1. **నాటడం**: రోగరహిత పంట కోసం టిష్యూ కల్చర్ మొక్కలను వాడండి. 1.8 మీ x 1.8 మీ దూరంలో నాటుకోండి.
                    2. **ఎరువులు**: అరటి ఎక్కువ ఎరువులు కోరుకుంటుంది. సమృద్ధిగా నత్రజని, పొటాషియంతో పాటు పశువుల ఎరువు వేయండి.
                    3. **నీరు**: పొలంలో తేమ ఎల్లప్పుడూ ఉండాలి. నీరు నిల్వ ఉండకుండా డ్రిప్ పద్ధతిని వాడండి.
                    4. **తెగుళ్లు**: సిగాటోకా ఆకుమచ్చ తెగులు నివారణకు ప్రొపికోనజోల్ (1 మి.లీ/లీటర్) పిచికారీ చేయండి.
                    5. **لاభాలు**: 11-12 నెలల్లోనే పంట చేతికి వస్తుంది. త్వరగా ఆదాయం ఇచ్చే ఉత్తమ వాణిజ్య పంట.""";
            case "kn" -> """
                    ನಮಸ್ಕಾರ ರೈತ ಬಂಧು! ಬಾಳೆಹಣ್ಣು ಬೇಸಾಯದ ಬಗ್ಗೆ ಪ್ರಮುಖ ಮಾಹಿತಿ:
                    
                    1. **ನಾಟಿ**: ಕೀಟ ಬಾಧೆ ಇಲ್ಲದಿರಲು ಮತ್ತು ಏಕರೂಪ ಬೆಳವಣಿಗೆಗೆ ಟಿಶ್ಯೂ ಕಲ್ಚರ್ ಗಿಡಗಳನ್ನು ಬಳಸಿ. 1.8 ಮೀ x 1.8 ಮೀ ಅಂತರವಿರಲಿ.
                    2. **ಗೊಬ್ಬರ**: ಬಾಳೆಗೆ ಹೆಚ್ಚು ಗೊಬ್ಬರ ಬೇಕು. ಹಟ್ಟಿಗೊಬ್ಬರದೊಂದಿಗೆ ನೈಟ್ರೋಜನ್ ಮತ್ತು ಪೊಟ್ಯಾಷಿಯಂ ಹೇರಳವಾಗಿ ಒದಗಿಸಿ.
                    3. **ನೀರಾವರಿ**: ಮಣ್ಣಿನಲ್ಲಿ ಸದಾ ತೇವಾಂಶವಿರಲಿ. ಹನಿ ನೀರಾವರಿ ಪದ್ಧತಿ ಅಳವಡಿಸುವುದು ಸೂಕ್ತ.
                    4. **ರೋಗಗಳು**: ಸಿಗಾಟೋಕಾ ಎಲೆ ಚುಕ್ಕೆ ರೋಗ ನಿಯಂತ್ರಣಕ್ಕೆ ಪ್ರೊಪಿಕೊನಾಜೋಲ್ (1 ಮಿ.ಲೀ/ಲೀ) ಸಿಂಪಡಿಸಿ.
                    5. **ಲಾಭ**: ಕೇವಲ 11-12 ತಿಂಗಳುಗಳಲ್ಲಿ ಉತ್ತಮ ಇಳುವರಿ ಬರುತ್ತದೆ. ಶೀಘ್ರ ಹಣ ನೀಡುವ ವಾಣಿಜ್ಯ ಬೆಳೆಯಾಗಿದೆ.""";
            default -> """
                    नमस्ते किसान भाई! केले की खेती के लिए विशिष्ट सलाह:
                    
                    1. **रोपण**: रोगमुक्त फसल के लिए टिशू कल्चर (Tissue Culture) पौधों का उपयोग करें। 1.8 मीटर x 1.8 मीटर की दूरी पर लगाएं।
                    2. **पोषण**: केला एक भारी पोषक तत्व खाने वाली फसल है। गोबर खाद के साथ प्रचुर नाइट्रोजन और पोटैशियम का प्रयोग करें।
                    3. **सिंचाई**: मिट्टी में निरंतर नमी रखें। जलभराव से बचते हुए ड्रिप सिंचाई का उपयोग करें।
                    4. **रोग**: सिगाटोका पत्ती धब्बा रोग से बचाव के लिए प्रोपिकोनाज़ोल (1 मिली/लीटर) का छिड़काव करें।
                    5. **मुनाफा**: 11-12 महीनों में उपज तैयार हो जाती है। जल्दी और निश्चित नकदी प्रवाह के कारण केला अत्यंत लोकप्रिय व्यवसाय है।""";
        };
    }

    // ── Mango Advisory ─────────────────────────────────────────────────────────

    private String getMangoAdvisory(String lang) {
        return switch (lang) {
            case "en" -> """
                    Hello farmer! Here is specific advice on Mango cultivation:
                    
                    1. **Spacing**: Standard spacing is 10m x 10m. For Ultra-High-Density orchards (UHDP), plant at 3m x 2m spacing using Amrapali or Kesar varieties.
                    2. **Soil**: Deep, rich, well-drained red loamy soils are ideal (pH 5.5-7.5).
                    3. **Flower Induction**: Apply Paclobutrazol in October to ensure regular flowering and reduce alternate bearing issues.
                    4. **Pests**: Mango Hopper and Powdery Mildew can damage flowers. Spray Imidacloprid and Sulfur before flowering.
                    5. **Profitability**: Mangoes are highly profitable long-term assets. Commercial export-quality varieties like Alphonso and Kesar fetch premium prices.""";
            case "mr" -> """
                    नमस्कार शेतकरी बंधू! आंबा लागवडीसंबंधी सविस्तर माहिती:
                    
                    1. **लागवड अंतर**: सर्वसाधारण लागवड १० x १० मीटर अंतरावर करतात. अतिसघन लागवडीसाठी (UHDP) आम्रपाली किंवा केशर जातींची ३ x २ मीटर अंतरावर लागवड करा.
                    2. **जमीन**: खोल, कसदार, चांगला निचरा असणारी तांबडी व पोयट्याची जमीन उत्तम ठरते.
                    3. **मोहर येणे**: दरवर्षी मोहर येण्यासाठी आणि नियमित उत्पन्नासाठी ऑक्टोबरमध्ये 'कलटार' (पॅक्लोब्युट्राझॉल) खताचा वापर करावा.
                    4. **कीड नियंत्रण**: आंब्यावरील तुडतुडे (Hopper) आणि भुरी रोगाच्या नियंत्रणासाठी फुलोऱ्यापूर्वी इमिडाक्लोप्रिड आणि सल्फर फवारा.
                    5. **नफा**: हापूस व केशर आंब्याला परदेशातही मोठी मागणी आहे. योग्य काढणी आणि पॅकिंग केल्यास थेट निर्यातीतून मोठा नफा मिळतो.""";
            case "te" -> """
                    నమస్కారం రైతు సోదరా! మామిడి సాగుకు ప్రత్యేక సూచనలు:
                    
                    1. **నాటడం**: సాధారణ పద్ధతిలో 10మీ x 10మీ దూరంలో నాటాలి. హై-డెన్సిటీ పద్ధతిలో అమ్రపాలి లేదా కేసర్ రకాలను 3మీ x 2మీ దూరంలో నాటుకోవచ్చు.
                    2. **నేల**: లోతైన మరియు ఎర్రటి ఇసుక నేలలు (pH 5.5-7.5) మామిడికి అనుకూలం.
                    3. **పూత ప్రేరణ**: ప్రతిసంవత్సరం సరిగ్గా పూత రావడం కోసం అక్టోబర్‌లో పాక్లోబ్యూట్రజోల్ వాడండి.
                    4. **తెగుళ్లు**: తేనెమంచు పురుగు (Hopper) నివారణకు పూత దశకు ముందే ఇమిడాక్లోప్రిడ్ పిచికారీ చేయండి.
                    5. **లాభాలు**: అల్ఫాన్సో, కేసర్ వంటి వాణిజ్య రకాల సాగు ద్వారా అత్యధిక విదేశీ ఆదాయం మరియు దీర్घाకాలిక లాభాలు లభిస్తాయి.""";
            case "kn" -> """
                    ನಮಸ್ಕಾರ ರೈತ ಬಂಧು! ಮಾವು ಬೇಸಾಯದ ಬಗ್ಗೆ ಪ್ರಮುಖ ಮಾಹಿತಿ:
                    
                    1. **ಅಂತರ**: ಸಾಮಾನ್ಯ ಅಂತರ 10ಮೀ x 10ಮೀ. ಅತಿ ಸಾಗುವಳಿ ತೋಟಗಾರಿಕೆಗೆ (UHDP) ಆಮ್ರಪಾಲಿ ಅಥವಾ ಕೇಸರ್ ತಳಿಗಳನ್ನು 3ಮೀ x 2ಮೀ ಅಂತರದಲ್ಲಿ ನಾಟಿ ಮಾಡಿ.
                    2. **ಮಣ್ಣು**: ಆಳವಾದ, ಕೆಂಪು ಮತ್ತು ಉತ್ತಮ ಒಳಚರಂಡಿ ಇರುವ ಮರಳು ಮಣ್ಣು ಅಗತ್ಯ.
                    3. **ಹೂ ಬಿಡಲು**: ಪ್ರತಿ ವರ್ಷ ಸಮರ್ಪಕವಾಗಿ ಹೂ ಬಿಡಲು ಅಕ್ಟೋಬರ್‌ನಲ್ಲಿ ಪ್ಯಾಕ್ಲೋಬುಟ್ರಜೋಲ್ ಬಳಸಿ.
                    4. **ಕೀಟ ನಿಯಂತ್ರಣ**: ಹೂವಿನ ಹಂತದಲ್ಲಿ ಮಾವಿನ ಜಿಗಿಹುಳು ಮತ್ತು ಬೂದಿ ರೋಗ ನಿಯಂತ್ರಿಸಲು ಇಮಿಡಾಕ್ಲೋಪ್ರಿಡ್ ಸಿಂಪಡಿಸಿ.
                    5. **ಲಾಭ**: ಉನ್ನತ ರಫ್ತು ಗುಣಮಟ್ಟದ ಆಲ್ಫಾನ್ಸೋ ಮತ್ತು ಕೇಸರ್ ತಳಿಗಳು ಮಾರುಕಟ್ಟೆಯಲ್ಲಿ ಗರಿಷ್ಠ ಬೆಲೆ ಮತ್ತು ಲಾಭ ತಂದುಕೊಡುತ್ತವೆ.""";
            default -> """
                    नमस्ते किसान भाई! आम की खेती के लिए विशिष्ट सलाह:
                    
                    1. **दूरी**: पारंपरिक दूरी 10 मीटर x 10 मीटर है। सघन बागवानी (UHDP) के लिए आम्रपाली या केसर किस्मों को 3 मीटर x 2 मीटर की दूरी पर लगाएं।
                    2. **मिट्टी**: गहरी, अच्छी जल निकास वाली लाल दोमट मिट्टी सबसे उपयुक्त है (pH 5.5-7.5)।
                    3. **पुष्पन**: नियमित बौर आने के लिए अक्टूबर में पैक्लोबुट्राजोल (Paclobutrazol) का प्रयोग करें।
                    4. **कीट**: भुनगा कीट (Mango Hopper) और पाउडर फफूंद से बौर बचाने के लिए इमिडाक्लोप्रिड का छिड़काव करें।
                    5. **मुनाफा**: हापुस (Alphonso) और केसर जैसी व्यावसायिक किस्मों की भारी मांग है। निर्यात के जरिए किसान लाखों रुपये कमा सकते हैं।""";
        };
    }

    // ── Sugarcane Advisory ─────────────────────────────────────────────────────

    private String getSugarcaneAdvisory(String lang) {
        return switch (lang) {
            case "en" -> """
                    Hello farmer! Here is specific advice on Sugarcane cultivation:
                    
                    1. **Soil & Climate**: Thrives in deep, rich clayey/loamy soils with good drainage (pH 6.5-7.5). Requires hot, humid weather.
                    2. **Planting**: Setts should be treated with Trichoderma before planting. Maintain furrow spacing of 90-120 cm.
                    3. **Irrigation**: Water intensive. Heavy watering required, especially during the vegetative growth phase. Use drip to save water.
                    4. **Fertilizers**: High requirement of Nitrogen, Phosphorus, and Potassium. Apply FYM (Manure) and urea in split doses.
                    5. **Harvesting**: Harvesting is done when lower leaves turn yellow and cane makes a metallic sound when tapped.""";
            case "mr" -> """
                    नमस्कार शेतकरी बंधू! ऊस लागवडीसंबंधी सविस्तर माहिती:
                    
                    1. **जमीन व हवामान**: उसासाठी खोल, सुपीक, पाण्याचा उत्तम निचरा असणारी काळी कसदार जमीन (pH ६.५ ते ७.५) योग्य आहे.
                    2. **लागवड**: बेणे प्रक्रिया करणे अत्यंत गरजेचे आहे. लागवडीपूर्वी बेणे ट्रायकोडर्मा किंवा बुरशीनाशकात बुडवावे. ३ ते ४ फूट रुंद सरी सोडावी.
                    3. **पाणी व्यवस्थापन**: उसाला भरपूर पाणी लागते. उन्हाळ्यात नियमित पाणी द्यावे. ठिबक सिंचनाने पाण्याची बचत व ऊस जाड होतो.
                    4. **खत व्यवस्थापन**: नत्र, स्फुरद व पालाशची योग्य मात्रा विभागून द्या. लागवडीवेळी भरपूर शेणखत किंवा कंपोस्ट खत टाकावे.
                    5. **काढणी**: साधारणपणे १० ते १२ महिन्यांत ऊस पक्व होतो, तेव्हा तोडणी करावी.""";
            case "te" -> """
                    నమస్కారం రైతు సోదరా! చెరకు సాగుకు ప్రత్యేక సూచనలు:
                    
                    1. **నేల మరియు వాతావరణం**: నీటి పారుదల సౌకర్యం గల లోతైన బంకమట్టి నేలలు అనుకూలం. వేడి మరియు తేమతో కూడిన వాతావరణం అవసరం.
                    2. **నాటడం**: విత్తన చెరకు ముక్కలను నాటే ముందు ట్రైకోడెర్మాతో శుద్ధి చేయాలి. వరుసల మధ్య 90-120 సెం.మీ దూరం ఉంచాలి.
                    3. **నీటి యాజమాన్యం**: చెరకు ఎక్కువ నీరు కోరుకుంటుంది. నీటి కొరత లేకుండా డ్రిప్ సిస్టమ్ వాడటం చాలా ప్రయోజనకరం.
                    4. **ఎరువులు**: నత్రజని, భాస్వరం మరియు పొటాష్ ఎరువులు అధిక మోతాదులో విడతల వారీగా అందించాలి.
                    5. **కోత**: కింద ఆకులు పసుపు రంగులోకి మారి, కాండం గట్టిపడినప్పుడు కోత ప్రారంభించాలి.""";
            case "kn" -> """
                    ನಮಸ್ಕಾರ ರೈತ ಬಂಧು! ಕಬ್ಬು ಬೇಸಾಯದ ಬಗ್ಗೆ ಪ್ರಮುಖ ಮಾಹಿತಿ:
                    
                    1. **ಮಣ್ಣು ಮತ್ತು ಹವಾಮಾನ**: ಫಲವತ್ತಾದ ಕಪ್ಪು ಮಣ್ಣು ಮತ್ತು ಒಳಚರಂಡಿ ವ್ಯವಸ್ಥೆ ಇದ್ದರೆ ಕಬ್ಬು ಉತ್ತಮವಾಗಿ ಬೆಳೆಯುತ್ತದೆ.
                    2. **ನಾಟಿ**: ನಾಟಿ ಮಾಡುವ ಮುನ್ನ ಕಬ್ಬಿನ ತುಂಡುಗಳನ್ನು ಟ್ರೈಕೋಡರ್ಮಾದೊಂದಿಗೆ ಸಂಸ್ಕರಿಸಿ. ಸಾಲುಗಳ ನಡುವೆ 90-120 ಸೆಂ.ಮೀ ಅಂತರವಿರಲಿ.
                    3. **ನೀರಾವರಿ**: ಇದಕ್ಕೆ ಹೆಚ್ಚು ನೀರಿನ ಅಗತ್ಯವಿದೆ. ನೀರು ಉಳಿಸಲು ಮತ್ತು ಇಳುವರಿ ಹೆಚ್ಚಿಸಲು ಹನಿ ನೀರಾವರಿ ಬಳಸಿ.
                    4. **ಗೊಬ್ಬರ**: ಸಾರಜನಕ, ರಂಜಕ ಮತ್ತು ಪೊಟ್ಯಾಷಿಯಂ ಗೊಬ್ಬರಗಳನ್ನು ಸಮರ್ಪಕವಾಗಿ ಒದಗಿಸಿ.
                    5. **ಕಟಾವು**: ಕಬ್ಬಿನ ಕೆಳಗಿನ ಎಲೆಗಳು ಹಳದಿಯಾಗಿ ಬದಲಾದಾಗ ಕಟಾವು ಮಾಡಬಹುದು.""";
            default -> """
                    नमस्ते किसान भाई! गन्ने की खेती के लिए विशिष्ट सलाह:
                    
                    1. **मिट्टी और जलवायु**: उत्तम जल निकासी वाली गहरी, उपजाऊ दोमट मिट्टी (pH 6.5-7.5) और गर्म-नम जलवायु आवश्यक है।
                    2. **रोपण**: बुआई से पहले गन्ने के टुकड़ों को ट्राइकोडेर्मा से उपचारित करें। कतारों के बीच 90-120 सेमी की दूरी रखें।
                    3. **सिंचाई**: अत्यधिक पानी की आवश्यकता होती है। पानी बचाने और पैदावार बढ़ाने के लिए ड्रिप सिंचाई अपनाएं।
                    4. **खाद**: नाइट्रोजन, फॉस्फोरस और पोटाश प्रचुर मात्रा में दें। गोबर की खाद अवश्य डालें।
                    5. **कटाई**: जब निचली पत्तियां पीली हो जाएं और गन्ना थपथपाने पर धातु जैसी आवाज दे, तब कटाई करें।""";
        };
    }

    // ── Cotton Advisory ────────────────────────────────────────────────────────

    private String getCottonAdvisory(String lang) {
        return switch (lang) {
            case "en" -> """
                    Hello farmer! Here is specific advice on Cotton cultivation:
                    
                    1. **Soil & Climate**: Grows best in deep black cotton soils (regur) which retain moisture. Requires warm climate with moderate rainfall.
                    2. **Sowing**: Sow during June-July with onset of monsoon. Spacing varies from 90x60 cm depending on variety.
                    3. **Pest Control**: Highly prone to Bollworm pests. Use Bt Cotton seeds to prevent infestation and spray Neem oil regularly.
                    4. **Fertilizers**: Requires balanced NPK. Excessive nitrogen can cause excessive vegetative growth instead of cotton bolls.
                    5. **Harvesting**: Pick cotton bolls when they are fully opened and dry. Avoid picking wet bolls to maintain fiber quality.""";
            case "mr" -> """
                    नमस्कार शेतकरी बंधू! कापूस लागवडीसंबंधी सविस्तर सल्ला:
                    
                    1. **जमीन व हवामान**: कापूस पिकासाठी ओलावा टिकवून ठेवणारी मध्यम ते खोल काळी जमीन (रेगूर माती) अत्यंत योग्य आहे.
                    2. **पेरणी**: मान्सूनच्या सुरुवातीला म्हणजेच जून ते जुलैच्या पहिल्या आठवड्यात पेरणी करावी. ९० x ६० सेंमी अंतरावर टोकण पद्धत वापरावी.
                    3. **कीड नियंत्रण**: बोंडअळीचा (Bollworm) प्रादुर्भाव रोखण्यासाठी बीटी कापूस बियाणे वापरा. निंबोळी अर्काची फवारणी करावी.
                    4. **खत व्यवस्थापन**: शिफारसीनुसार नत्र, स्फुरद आणि पालाशची संतुलित मात्रा द्या. जास्त नत्र दिल्यास पिकाची फक्त वाढ होते, बोंडे कमी लागतात.
                    5. **काढणी (वेचणी)**: बोंडे पूर्णपणे उमलल्यावर कोरड्या हवामानात कापसाची वेचणी करावी. ओला कापूस वेचू नये.""";
            case "te" -> """
                    నమస్కారం రైతు సోదరా! పత్తి సాగుకు ప్రత్యేక సూచనలు:
                    
                    1. **నేల మరియు వాతావరణం**: తేమను నిల్వ ఉంచే నల్ల రేగడి నేలలు అత్యంత అనుకూలం. వేడి వాతావరణం ఉండాలి.
                    2. **విత్తడం**: జూన్-జూలై నెలల్లో వర్షాలు పడగానే విత్తాలి. రకాన్ని బట్టి 90x60 సెం.మీ దూరం పాటించాలి.
                    3. **పురుగుల నివారణ**: పత్తికి గులాబీ రంగు కాయతొలిచే పురుగు ప్రమాదం ఎక్కువ. నివారణకు బీటీ పత్తి వాడటం మరియు వేప నూనె పిచికారీ చేయడం చేయాలి.
                    4. **ఎరువులు**: సమతుల్య NPK ఎరువులు వేయాలి. నత్రజని అధికంగా వేస్తే మొక్క ఎత్తు పెరుగుతుంది కానీ కాయలు తక్కువగా వస్తాయి.
                    5. **కోత**: పత్తి కాయలు పూర్తిగా విచ్చుకుని ఎండిన తర్వాత మాత్రమే ఏరాలి. తడి పత్తిని ఏరవద్దు.""";
            case "kn" -> """
                    ನಮಸ್ಕಾರ ರೈತ ಬಂಧು! ಹತ್ತಿ ಬೇಸಾಯದ ಬಗ್ಗೆ ಪ್ರಮುಖ ಮಾಹಿತಿ:
                    
                    1. **ಮಣ್ಣು ಮತ್ತು ಹವಾಮಾನ**: ತೇವಾಂಶವನ್ನು ಹಿಡಿದಿಟ್ಟುಕೊಳ್ಳುವ ಕಪ್ಪು ಹತ್ತಿ ಮಣ್ಣು ಮತ್ತು ಬೆಚ್ಚಗಿನ ಒಣ ಹವಾಮಾನ ಸೂಕ್ತ.
                    2. **ಬಿತ್ತನೆ**: ಜೂನ್-ಜುಲೈ ತಿಂಗಳಲ್ಲಿ ಮಳೆ ಪ್ರಾರಂಭವಾದ ತಕ್ಷಣ ಬಿತ್ತನೆ ಮಾಡಿ. 90x60 ಸೆಂ.ಮೀ ಅಂತರವಿರಲಿ.
                    3. **ಕೀಟ ನಿಯಂತ್ರಣ**: ಹತ್ತಿಗೆ ಕಾಯಿ ಕೊರೆಯುವ ಹುಳು ಬಾಧೆ ಹೆಚ್ಚು. ಬಿಟಿ ಹತ್ತಿ ಬೀಜಗಳನ್ನು ಬಳಸಿ ಹಾಗೂ ನಿಯಮಿತವಾಗಿ ಬೇವಿನ ಎಣ್ಣೆ ಸಿಂಪಡಿಸಿ.
                    4. **ಗೊಬ್ಬರ**: ಸಮತೋಲಿತ ಪ್ರಮಾಣದಲ್ಲಿ NPK ಗೊಬ್ಬರವನ್ನು ಒದಗಿಸಿ. ಸಾರಜನಕ ಹೆಚ್ಚಾದರೆ ಹತ್ತಿ ಕಾಯಿಗಳು ಕಡಿಮೆಯಾಗುತ್ತವೆ.
                    5. **ಕಟಾವು**: ಹತ್ತಿ ಕಾಯಿಗಳು ಸಂಪೂರ್ಣವಾಗಿ ಒಣಗಿ ಅರಳಿದಾಗ ಬಿಡಿಸಿ. ಒದ್ದೆಯಾದ ಹತ್ತಿ ಬಿಡಿಸಬೇಡಿ.""";
            default -> """
                    नमस्ते किसान भाई! कपास की खेती के लिए विशिष्ट सलाह:
                    
                    1. **मिट्टी और जलवायु**: नमी बनाए रखने वाली गहरी काली मिट्टी (रेगुर) सबसे उपयुक्त है। मध्यम वर्षा और गर्म जलवायु की आवश्यकता होती है।
                    2. **बुआई**: जून-जुलाई में मानसून की शुरुआत के साथ बुआई करें। पौधों के बीच उचित दूरी (90x60 सेमी) रखें।
                    3. **कीट नियंत्रण**: कपास में गुलाबी सुंडी (Bollworm) का प्रकोप अधिक होता है। बीटी कपास (Bt Cotton) का उपयोग करें और नीम तेल का छिड़काव करें।
                    4. **खाद**: संतुलित एनपीके (NPK) उर्वरकों का प्रयोग करें। नाइट्रोजन की अधिक मात्रा से बचें।
                    5. **चुनाई**: जब कपास के डोडे पूरी तरह खिलकर सूख जाएं, तभी चुनाई करें। गीली रुई की चुनाई न करें।""";
        };
    }

    // ── Coffee, Tea & Spices Advisory ─────────────────────────────────────────

    private String getCoffeeTeaAdvisory(String lang) {
        return switch (lang) {
            case "en" -> """
                    Hello farmer! Advice on Coffee, Tea, and Spices (Ginger, Garlic, Cardamom) cultivation:
                    
                    1. **Site Selection**: Requires hilly slopes with partial shade and deep, acidic, humus-rich organic soils.
                    2. **Planting**: For Coffee/Tea, shade trees (like Silver Oak) must be planted first. For Ginger/Garlic, plant on raised beds.
                    3. **Water**: High humidity and regular rainfall is needed, but water must never stand around the roots.
                    4. **Pruning**: Regular pruning of coffee/tea bushes maintains ideal plucking height and stimulates fresh leaf buds.
                    5. **High Value**: These crops command high domestic and international export prices. Organic certification guarantees maximum profit.""";
            case "mr" -> """
                    नमस्कार शेतकरी बंधू! कॉफी, चहा आणि मसाल्याच्या पिकांसंबंधी (आले, लसूण, वेलची) सल्ला:
                    
                    1. **जमीन व जागा**: डोंगराळ उताराची जमीन, सावली आणि सेंद्रिय पदार्थांनी समृद्ध आम्लयुक्त जमीन आवश्यक आहे.
                    2. **लागवड**: कॉफी/चहासाठी सावली देणारी झाडे लावावीत. आले आणि लसूण गादीवाफ्यावर लावावे.
                    3. **पाणी व्यवस्थापन**: पिकाला दमट हवामान व नियमित पाऊस लागतो, पण पाणी साचून राहणे पिकाला घातक ठरते.
                    4. **छाटणी (Pruning)**: झाडांची वेळोवेळी छाटणी केल्याने झाडांना नवीन फूट मिळते व पाने तोडणे सोपे जाते.
                    5. **नफा**: ही पिके आंतरराष्ट्रीय बाजारपेठेत विकली जातात. सेंद्रिय लागवड केल्यास दुप्पट नफा मिळतो.""";
            case "te" -> """
                    నమస్కారం రైతు సోదరా! కాఫీ, టీ మరియు సుగంధ ద్రవ్యాలు (అల్లం, వెల్లుల్లి, యాలకులు) సాగుకు ప్రత్యేక సూచనలు:
                    
                    1. **స్థల ఎంపిక**: కొండ వాలు ప్రాంతాలు, పాక్షిక నీడ మరియు సేంద్రీయ పదార్థాలు గల ఆమ్ల నేలలు అత్యంత అనుకూలం.
                    2. **నాటడం**: కాఫీ/టీ తోటలకు నీడ నిచ్చే చెట్లను ముందే నాటాలి. అల్లం/వెల్లుల్లి ఎత్తైన మడుల పై నాటాలి.
                    3. **నీరు**: తేమతో కూడిన వాతావరణం మరియు సక్రమంగా వర్షపాతం ఉండాలి, కానీ వేర్ల వద్ద నీరు నిల్వ ఉండరాదు.
                    4. **కత్తిరింపు**: కొమ్మలను క్రమం తప్పకుండా కత్తిరించడం వల్ల కొత్త చిగుళ్లు త్వరగా వస్తాయి.
                    5. **లాభాలు**: ఈ పంటలకు జాతీయ మరియు అంతర్జాతీయ మార్కెట్లో అధిక ధరలు లభిస్తాయి.""";
            case "kn" -> """
                    ನಮಸ್ಕಾರ ರೈತ ಬಂಧು! ಕಾಫಿ, ಚಹಾ ಮತ್ತು ಸಾಂಬಾರ ಪದಾರ್ಥಗಳ (ಶುಂಠಿ, ಬೆಳ್ಳುಳ್ಳಿ, ಏಲಕ್ಕಿ) ಬೇಸಾಯದ ಪ್ರಮುಖ ಮಾಹಿತಿ:
                    
                    1. **ಜಾಗದ ಆಯ್ಕೆ**: ಗುಡ್ಡಗಾಡು ಪ್ರದೇಶ, ಸ್ವಲ್ಪ ನೆರಳು ಮತ್ತು ಫಲವತ್ತಾದ ಸಾವಯವ ಆಮ್ಲ ಮಣ್ಣು ಸೂಕ್ತ.
                    2. **ನಾಟಿ**: ಕಾಫಿ/ಚಹಾ ತೋಟಗಳಿಗೆ ನೆರಳು ನೀಡುವ ಮರಗಳನ್ನು ಮುಂಚಿತವಾಗಿ ಬೆಳೆಸಿ. ಶುಂಠಿ/ಬೆಳ್ಳುಳ್ಳಿಗಳನ್ನು ಏರುಮಡಿಗಳಲ್ಲಿ ನಾಟಿ ಮಾಡಿ.
                    3. **ನೀರಾವರಿ**: ವಾತಾವರಣದಲ್ಲಿ ತೇವಾಂಶವಿರಲಿ, ಆದರೆ ಬೇರುಗಳ ಬಳಿ ನೀರು ನಿಲ್ಲದಂತೆ ಜಾಗ್ರತೆ ವಹಿಸಿ.
                    4. **ಕತ್ತರಿಕೆ**: ಗಿಡಗಳನ್ನು ನಿಯಮಿತವಾಗಿ ಕತ್ತರಿಸುವುದರಿಂದ ಹೊಸ ಚಿಗುರುಗಳು ಬರುತ್ತವೆ.
                    5. **ಲಾಭ**: ಇವುಗಳು ರಫ್ತು ಆಧಾರಿತ ಬೆಳೆಗಳಾಗಿದ್ದು, ಮಾರುಕಟ್ಟೆಯಲ್ಲಿ ಗರಿಷ್ಠ ಬೆಲೆ ಪಡೆಯುತ್ತವೆ.""";
            default -> """
                    नमस्ते किसान भाई! कॉफी, चाय और मसालों (अदरक, लहसुन, इलायची) की खेती के लिए विशिष्ट सलाह:
                    
                    1. **स्थान चयन**: ढलान वाले पहाड़ी क्षेत्र, आंशिक छाया और अम्लीय, जैविक तत्वों से भरपूर मिट्टी आवश्यक है।
                    2. **रोपण**: कॉफी/चाय के लिए छायादार पेड़ (जैसे सिल्वर ओक) पहले लगाएं। अदरक/लहसुन को उठी हुई क्यारियों पर लगाएं।
                    3. **सिंचाई**: उच्च नमी और नियमित हल्की वर्षा की आवश्यकता होती है, लेकिन जड़ों के पास पानी कभी जमा नहीं होना चाहिए।
                    4. **कटाई**: कॉफी/चाय की झाड़ियों की नियमित छंटाई करें ताकि नई पत्तियां और कलियां आ सकें।
                    5. **उच्च मूल्य**: इन फसलों की बाजार में बहुत ऊंची कीमत होती है। जैविक खेती से विदेशों में निर्यात कर भारी मुनाफा कमाया जा सकता है।""";
        };
    }

    // ── Oilseeds Advisory ──────────────────────────────────────────────────────

    private String getGroundnutSoybeanAdvisory(String lang) {
        return switch (lang) {
            case "en" -> """
                    Hello farmer! Advice on Oilseed crops (Groundnut, Soybean, Mustard):
                    
                    1. **Soil**: Groundnuts require loose sandy-loam (so pods can grow underground). Mustard and Soybean prefer rich loamy soils.
                    2. **Seed Treatment**: Treat seeds with Rhizobium culture to fix nitrogen and prevent seedling rot.
                    3. **Water**: Groundnuts and Soybean are monsoon crops. Mustard is a winter (Rabi) crop requiring cool weather and 2-3 light irrigations.
                    4. **Gypsum**: Apply Gypsum (200kg/acre) to groundnuts during flowering to ensure proper pod and kernel development.
                    5. **Profitability**: Oilseeds are in constant high demand. Crop rotation of oilseeds restores soil nitrogen, saving future fertilizer costs.""";
            case "mr" -> """
                    नमस्कार शेतकरी बंधू! तेलबिया पिकांसंबंधी (भुईमूग, सोयाबीन, मोहरी) सविस्तर माहिती:
                    
                    1. **जमीन**: भुईमुगासाठी हलकी, भुसभुशीत व वाळूमिश्रित जमीन हवी (जेणेकरून आऱ्या सहज जमिनीत जातील). सोयाबीन व मोहरीसाठी मध्यम ते भारी जमीन लागते.
                    2. **बीजप्रक्रिया**: पेरणीपूर्वी बियाणास रायझोबियम जीवाणू संवर्धकाची प्रक्रिया अवश्य करा.
                    3. **पाणी व्यवस्थापन**: भुईमूग व सोयाबीन खरीप हंगामात येतात. मोहरी हे हिवाळी (रब्बी) पीक असून त्याला २ ते ३ हलक्या पाण्याच्या पाळ्या लागतात.
                    4. **जिप्समचा वापर**: भुईमुगाला आऱ्या सुटताना एकरी २०० किलो जिप्सम द्यावे, यामुळे शेंगा पोसण्यास मदत होते.
                    5. **नफा**: तेलबियांची मागणी बाजारपेठेत मोठी आहे. द्विदल तेलबिया पिकांमुळे जमिनीचा सुपीकपणा वाढतो व खताचा खर्च वाचतो.""";
            case "te" -> """
                    నమస్కారం రైతు సోదరా! నూనెగింజల పంటల (వేరుశనగ, సోయాబీన్, ఆవాలు) సాగుకు ప్రత్యేక సూచనలు:
                    
                    1. **నేల**: వేరుశనగ సాగుకు తేలికపాటి ఇసుక నేలలు అవసరం (కాయలు భూమిలో పెరగడానికి). ఆవాలు, సోయాబీన్ కు లోమ్ నేలలు అనుకూలం.
                    2. **విత్తన శుద్ధి**: విత్తనాలను రైజోబియం కల్చర్‌తో శుద్ధి చేయడం ద్వారా నత్రజని స్థిరీకరణ జరుగుతుంది.
                    3. **నీటి యాజమాన్యం**: వేరుశనగ మరియు సోయాబీన్ వర్షాకాలంలో పండుతాయి. ఆవాలు శీతాకాల పంట, దీనికి 2-3 తేలికపాటి తడులు సరిపోతాయి.
                    4. **జిప్సం**: వేరుశనగ పూత దశలో ఎకరాకు 200 కిలోల జిప్సం వేయాలి, దీనివల్ల కాయలు గట్టిగా ఎదుగుతాయి.
                    5. **లాభాలు**: నూనెగింజలకు నిరంతరం డిమాండ్ ఉంటుంది. వీటి సాగు వల్ల మట్టి సారవంతం అవుతుంది.""";
            case "kn" -> """
                    ನಮಸ್ಕಾರ ರೈತ ಬಂಧು! ತೈಲಬೀಜ ಬೆಳೆಗಳ (ಕಡಲೆಕಾಯಿ, ಸೋಯಾಬೀನ್, ಸಾಸಿವೆ) ಬಗ್ಗೆ ಪ್ರಮುಖ ಮಾಹಿತಿ:
                    
                    1. **ಮಣ್ಣು**: ಕಡಲೆಕಾಯಿಗೆ ಮರಳು ಮಿಶ್ರಿತ ಹಗುರವಾದ ಮಣ್ಣು ಸೂಕ್ತ. ಸಾಸಿವೆ ಮತ್ತು ಸೋಯಾಬೀನ್‌ಗೆ ಫಲವತ್ತಾದ ಮಣ್ಣು ಬೇಕು.
                    2. **ಬೀಜೋಪಚಾರ**: ಬಿತ್ತನೆಗೆ ಮುನ್ನ ಬೀಜಗಳನ್ನು ರೈಜೋಬಿಯಂನೊಂದಿಗೆ ಉಪಚರಿಸಿ.
                    3. **ನೀರಾವರಿ**: ಕಡಲೆಕಾಯಿ ಮತ್ತು ಸೋಯಾಬೀನ್ ಮಳೆಗಾಲದ ಬೆಳೆಗಳಾಗಿದ್ದು, ಸಾಸಿವೆ ಚಳಿಗಾಲದ ಬೆಳೆಯಾಗಿದ್ದು ಇದಕ್ಕೆ 2-3 ಬಾರಿ ತಿಳಿ ನೀರಾವರಿ ಸಾಕು.
                    4. **ಜಿಪ್ಸಮ್**: ಕಡಲೆಕಾಯಿ ಹೂ ಬಿಡುವ ಹಂತದಲ್ಲಿ ಎಕರೆಗೆ 200 ಕೆಜಿ ಜಿಪ್ಸಮ್ ಒದಗಿಸುವುದರಿಂದ ಕಾಯಿಗಳು ಚೆನ್ನಾಗಿ ತುಂಬಿಕೊಳ್ಳುತ್ತವೆ.
                    5. **ಲಾಭ**: ತೈಲಬೀಜಗಳಿಗೆ ಮಾರುಕಟ್ಟೆಯಲ್ಲಿ ಸದಾ ಹೆಚ್ಚಿನ ಬೇಡಿಕೆಯಿದ್ದು, ಇವುಗಳ ಕೃಷಿಯಿಂದ ಭೂಮಿಯ ಫಲವತ್ತತೆ ಹೆಚ್ಚುತ್ತದೆ.""";
            default -> """
                    नमस्ते किसान भाई! तिलहन फसलों (मूंगफली, सोयाबीन, सरसों) के लिए विशिष्ट सलाह:
                    
                    1. **मिट्टी**: मूंगफली के लिए भुरभुरी बलुई-दोमट मिट्टी (ताकि नीचे दाने बढ़ सकें) और सरसों/सोयाबीन के लिए उपजाऊ दोमट मिट्टी उपयुक्त है।
                    2. **बीज उपचार**: बोने से पहले बीजों को राइजोबियम (Rhizobium) कल्चर से उपचारित करें ताकि नाइट्रोजन स्थिरीकरण हो सके।
                    3. **सिंचाई**: मूंगफली और सोयाबीन खरीफ की फसलें हैं। सरसों रबी (सर्दियों) की फसल है जिसे 2-3 हल्की सिंचाई की आवश्यकता होती है।
                    4. **जिप्सम**: मूंगफली में फूल आते समय जिप्सम (200 किग्रा/एकड़) डालें, इससे दाने मजबूत बनते हैं।
                    5. **मुनाफा**: खाद्य तेलों की मांग हमेशा बनी रहती है। तिलहन फसलों से फसल चक्र अपनाने से मिट्टी की उपजाऊ क्षमता बढ़ती है।""";
        };
    }

    // ── Pulses Advisory ────────────────────────────────────────────────────────

    private String getPulsesAdvisory(String lang) {
        return switch (lang) {
            case "en" -> """
                    Hello farmer! Advice on Pulse crops (Gram, Chickpea, Lentil, Pigeon Pea, Mung):
                    
                    1. **Soil**: Prefers well-drained sandy loam or clay loam soils. Avoid high nitrogen fertilizers since pulses fix their own nitrogen.
                    2. **Inoculation**: Inoculate seeds with Rhizobium and Phosphobacter bacteria cultures to boost crop growth and root nodules.
                    3. **Water**: Low water requirement. Crucial irrigation stages are branching and pod formation. Avoid water logging.
                    4. **Pests**: Pod Borer is the main pest. Use pheromone traps and spray Neem extract during early flowering.
                    5. **Profitability**: High market demand due to protein content. Pulse crops improve soil health naturally, preparing fields for next rotation.""";
            case "mr" -> """
                    नमस्कार शेतकरी बंधू! कडधान्य पिकांसंबंधी (चना/हरभरा, तूर, मसूर, मूग, उडीद) सविस्तर सल्ला:
                    
                    1. **जमीन**: कडधान्यांसाठी मध्यम ते भारी, पाण्याचा निचरा होणारी जमीन निवडा. या पिकांच्या मुळांवर गाठी असतात ज्या हवेतील नत्र शोषतात, त्यामुळे नत्रयुक्त खते कमी द्यावीत.
                    2. **बीजप्रक्रिया**: पेरणीपूर्वी रायझोबियम आणि स्फुरद विरघळवणाऱ्या जिवाणूंची (PSB) बीजप्रक्रिया आवर्जून करा.
                    3. **पाणी**: कडधान्यांना कमी पाणी लागते. फुले येताना आणि घाटे/शेंगा भरताना पाण्याच्या हलक्या पाळ्या द्या. पाणी साचू देऊ नका.
                    4. **कीड नियंत्रण**: घाटे अळीचा (Pod Borer) प्रादुर्भाव रोखण्यासाठी कामगंध सापळे लावा आणि फुलोऱ्याच्या सुरुवातीला निंबोळी अर्काची फवारणी करा.
                    5. **नफा**: प्रथिनांचा मुख्य स्त्रोत असल्याने डाळींना बाजारात नेहमी चांगला भाव मिळतो. कडधान्यांमुळे जमिनीचा पोत सुधारतो.""";
            case "te" -> """
                    నమస్కారం రైతు సోదరా! పప్పుధాన్యాల పంటల (శనగలు, కందులు, పెసర, మినుములు) సాగుకు ప్రత్యేక సూచనలు:
                    
                    1. **నేల**: నీరు నిల్వ ఉండని సారవంతమైన లోమ్ నేలలు అనుకూలం. వీటికి నత్రజని ఎరువుల వాడకం తక్కువగా ఉండాలి.
                    2. **విత్తన శుద్ధి**: వేర్ల అభివృద్ధి కోసం రైజోబియం మరియు భాస్వరం కరిగించే బ్యాక్టీరియాతో విత్తన శుద్ధి చేయాలి.
                    3. **నీరు**: తక్కువ నీటితో పండుతాయి. కొమ్మలు మరియు కాయలు వచ్చే దశల్లో తడులు ఇవ్వాలి. నీరు నిల్వ ఉండకుండా చూసుకోవాలి.
                    4. **తెగుళ్లు**: కాయతొలిచే పురుగు నివారణకు పూత దశలో లింగాకర్షక బుట్టలు అమర్చడం, వేప కషాయం పిచికారీ చేయడం ప్రయోజనకరం.
                    5. **లాభాలు**: పప్పుధాన్యాలకు ఎప్పుడూ మంచి ధర ఉంటుంది. ఈ పంటలు భూమి యొక్క నత్రజని శాతాన్ని పెంచి నేలను సారవంతం చేస్తాయి.""";
            case "kn" -> """
                    ನಮಸ್ಕಾರ ರೈತ ಬಂಧು! ಬೇಳೆಕಾಳು ಬೆಳೆಗಳ (ಕಡಲೆ, ತೊಗರಿ, ಹೆಸರು, ಉದ್ದು, ಮಸೂರ್) ಬಗ್ಗೆ ಪ್ರಮುಖ ಮಾಹಿತಿ:
                    
                    1. **ಮಣ್ಣು**: ಒಳಚರಂಡಿ ವ್ಯವಸ್ಥೆ ಇರುವ ಕೆಂಪು ಮರಳು ಮಿಶ್ರಿತ ಮಣ್ಣು ಸೂಕ್ತ. ಸಾರಜನಕದ ಗೊಬ್ಬರ ಕಡಿಮೆ ಬಳಸಿ.
                    2. **ಬೀಜೋಪಚಾರ**: ಗಿಡಗಳ ಬೇರಿನ ರಕ್ಷಣೆಗಾಗಿ ಬಿತ್ತನೆಗೆ ಮುನ್ನ ರೈಜೋಬಿಯಂ ಮತ್ತು ಪಿಎಸ್ಬಿಯಿಂದ ಬೀಜೋಪಚಾರ ಮಾಡಿ.
                    3. **ನೀರಾವರಿ**: ನೀರಾವರಿ ಅಗತ್ಯ ಕಡಿಮೆ. ಹೂ ಬಿಡುವ ಮತ್ತು ಕಾಯಿ ಕಟ್ಟುವ ಹಂತದಲ್ಲಿ ಮಣ್ಣಿನಲ್ಲಿ ತೇವಾಂಶವಿರಲಿ. ನೀರು ನಿಲ್ಲದಂತೆ ನೋಡಿಕೊಳ್ಳಿ.
                    4. **ಕೀಟ ನಿಯಂತ್ರಣ**: ಕಾಯಿ ಕೊರೆಯುವ ಹುಳು ಬಾಧೆ ತಡೆಯಲು ಫೆರಮೋನ್ ಬಲೆಗಳನ್ನು ಬಳಸಿ ಮತ್ತು ಆರಂಭಿಕ ಹಂತದಲ್ಲಿ ಬೇವಿನ ಕಷಾಯ ಸಿಂಪಡಿಸಿ.
                    5. **ಲಾಭ**: ಬೇಳೆಕಾಳುಗಳಿಗೆ ಸದಾ ಮಾರುಕಟ್ಟೆಯಲ್ಲಿ ಉತ್ತಮ ಬೇಡಿಕೆಯಿರುತ್ತದೆ ಮತ್ತು ಇವು ಮಣ್ಣಿನ ನೈಸರ್ಗಿಕ ಫಲವತ್ತತೆಯನ್ನು ಹೆಚ್ಚಿಸುತ್ತವೆ.""";
            default -> """
                    नमस्ते किसान भाई! दलहन फसलों (चना, अरहर, मसूर, मूंग, उड़द) के लिए विशिष्ट सलाह:
                    
                    1. **मिट्टी**: अच्छे जल निकास वाली बलुई दोमट या मटियार दोमट मिट्टी उपयुक्त है। नाइट्रोजन खाद का कम उपयोग करें क्योंकि ये खुद नाइट्रोजन बनाती हैं।
                    2. **बीज उपचार**: बेहतर जड़ों के विकास के लिए बीजों को राइजोबियम (Rhizobium) और पीएसबी (PSB) से उपचारित करें।
                    3. **सिंचाई**: इन्हें कम पानी की आवश्यकता होती है। शाखाएं बनते समय और फली बनते समय सिंचाई अवश्य करें। जलभराव न होने दें।
                    4. **कीट**: फली छेदक (Pod Borer) सबसे बड़ा दुश्मन है। फेरोमोन ट्रैप (Pheromone Traps) लगाएं और फूल आने की शुरुआत में नीम का काढ़ा छिड़कें।
                    5. **मुनाफा**: दालों की कीमतें हमेशा अधिक रहती हैं। ये फसलें भूमि की उर्वरा शक्ति बढ़ाती हैं जिससे अगली फसल में यूरिया का खर्च आधा हो जाता है।""";
        };
    }

    // ── Vegetables Advisory ────────────────────────────────────────────────────

    private String getVegetablesAdvisory(String lang) {
        return switch (lang) {
            case "en" -> """
                    Hello farmer! Here is specific advice on Vegetable cultivation (Tomato, Onion, Potato, etc.):
                    
                    1. **Tomato**: Requires stakes or supports for vine growth. Spray organic neem oil to control fruit borers. Use compost regularly.
                    2. **Onion**: Grow nurseries first, then transplant. Maintain shallow watering. Stop irrigation 15 days before harvest to prevent rotting.
                    3. **Potato**: Plant tubers in ridges and furrows. Earth up the soil around plants at 35 days. Watch out for late blight disease.
                    4. **Compost & Care**: Vegetables grow fast (2-4 months). Apply cow dung manure and vermicompost for high yields.
                    5. **Profitability**: Short-duration vegetables ensure constant weekly cash flow and are highly profitable when sold in local urban markets.""";
            case "mr" -> """
                    नमस्कार शेतकरी बंधू! भाजीपाला लागवडीसंबंधी (टोमॅटो, कांदा, बटाटा इ.) सविस्तर माहिती:
                    
                    1. **टोमॅटो**: वेलींना बांबू आणि सुतळीने आधार (Staking) द्या. फळे पोखरणाऱ्या अळीसाठी निंबोळी अर्काची फवारणी करा.
                    2. **कांदा**: गादीवाफ्यावर रोपे तयार करून पुनर्लागवड करा. काढणीच्या १५ दिवस आधी पाणी देणे बंद करा जेणेकरून कांदा सडणार नाही.
                    3. **बटाटा**: बटाटा लागवड सऱ्या-वरंब्यांवर करा. ३५ ते ४० दिवसांनी झाडाच्या बुंध्याशी माती भर द्या (Earthing-up).
                    4. **खत व निगा**: भाजीपाला २-३ महिन्यांत निघतो. यासाठी कंपोस्ट किंवा वर्मीकंपोस्ट (गांडूळ खत) जास्त प्रमाणात वापरा.
                    5. **नफा**: कमी कालावधीत नियमित व दर आठवड्याला हमखास पैसा देणारा हा उत्तम शेती पूरक व्यवसाय आहे.""";
            case "te" -> """
                    నమస్కారం రైతు సోదరా! కూరగాయల సాగు (టమోటా, ఉల్లిపాయ, బంగాళాదుంప) పై ప్రత్యేక సూచనలు:
                    
                    1. **టమోటా**: మొక్కలు పడిపోకుండా కర్రల సాయం అందించండి. కాయతొలిచే పురుగు నివారణకు వేప నూనె పిచికారీ చేయండి.
                    2. **ఉల్లిపాయ**: నారు పోసి నాటుకోండి. ఉల్లిగడ్డలు కుళ్ళిపోకుండా ఉండటానికి పంట కోతకు 15 రోజుల ముందు నీరు ఆపివేయండి.
                    3. **బంగాళాదుంప**: దుంపలను బోదెలపై నాటాలి. 35 రోజుల వద్ద వేర్ల చుట్టూ మట్టిని ఎగదోయాలి (Earthing-up).
                    4. **పోషణ**: కూరగాయలు 2-4 నెలల్లోనే చేతికి వస్తాయి. అధిక దిగుబడి కోసం పశువుల ఎరువు మరియు వర్మీకంపోస్ట్ వాడండి.
                    5. **లాభాలు**: తక్కువ కాలంలో నిరంతర వారపు ఆదాయాన్ని సమకూర్చే అత్యంత లాభదాయకమైన రంగం కూరగాయల సాగు.""";
            case "kn" -> """
                    ನಮಸ್ಕಾರ ರೈತ ಬಂಧು! ತರಕಾರಿ ಬೇಸಾಯದ (ಟೊಮೆಟೊ, ಈರುಳ್ಳಿ, ಆಲೂಗಡ್ಡೆ ಇತ್ಯಾದಿ) ಬಗ್ಗೆ ಪ್ರಮುಖ ಮಾಹಿತಿ:
                    
                    1. **ಟೊಮೆಟೊ**: ಟೊಮೆಟೊ ಗಿಡಗಳಿಗೆ ಕೋಲುಗಳ ಆಧಾರ ನೀಡಿ. ಹಣ್ಣು ಕೊರೆಯುವ ಹುಳು ತಡೆಗೆ ಬೇವಿನ ಎಣ್ಣೆ ಸಿಂಪಡಿಸಿ.
                    2. **ಈರುಳ್ಳಿ**: ಮೊದಲು ಸಸಿ ಮಡಿಗಳನ್ನು ಸಿದ್ಧಪಡಿಸಿ ನಾಟಿ ಮಾಡಿ. ಕೊಳೆಯುವುದನ್ನು ತಡೆಯಲು ಕಟಾವಿಗೆ 15 ದಿನ ಮೊದಲು ನೀರು ನಿಲ್ಲಿಸಿ.
                    3. **ಆಲೂಗಡ್ಡೆ**: ಆಲೂಗಡ್ಡೆ ಗೆಡ್ಡೆಗಳನ್ನು ದಿಬ್ಬಗಳಲ್ಲಿ ನಾಟಿ ಮಾಡಿ. 35ನೇ ದಿನದಲ್ಲಿ ಬುಡಕ್ಕೆ ಮಣ್ಣು ಮುಚ್ಚಿ (Earthing-up).
                    4. **ಪೋಷಣೆ**: ತರಕಾರಿಗಳು 2-4 ತಿಂಗಳಲ್ಲಿ ಕಟಾವಿಗೆ ಬರುತ್ತವೆ. ಇಳುವರಿ ಹೆಚ್ಚಿಸಲು ಸಾವಯವ ಗೊಬ್ಬರ ಮತ್ತು ಎರೆಹುಳು ಗೊಬ್ಬರ ಬಳಸಿ.
                    5. **ಲಾಭ**: ತರಕಾರಿ ಕೃಷಿಯು ಪ್ರತೀ ವಾರ ನಿರಂತರವಾಗಿ ಕೈಗೆ ಹಣ ನೀಡುವ ಅತ್ಯುತ್ತಮ ಲಾಭದಾಯಕ ಕೃಷಿಯಾಗಿದೆ.""";
            default -> """
                    नमस्ते किसान भाई! सब्जियों की खेती (टमाटर, प्याज, आलू आदि) के लिए विशिष्ट सलाह:
                    
                    1. **टमाटर**: बेलों को सहारा देने के लिए लकड़ी या धागे का सहारा (Staking) दें। फल छेदक कीट के नियंत्रण के लिए नीम तेल का छिड़काव करें।
                    2. **प्याज**: पहले नर्सरी तैयार करें, फिर रोपण करें। हल्की सिंचाई करें और सड़ने से बचाने के लिए खुदाई से 15 दिन पहले पानी बंद कर दें।
                    3. **आलू**: कंदों को मेड़ों (Ridges) पर रोपें। 35 दिनों पर मिट्टी चढ़ाने (Earthing-up) का कार्य करें। झुलसा रोग (Blight) का ध्यान रखें।
                    4. **देखभाल**: सब्जियां 2-4 महीनों में तैयार हो जाती हैं। अधिक पैदावार के लिए गोबर खाद और केंचुआ खाद का प्रयोग करें।
                    5. **मुनाफा**: कम अवधि की सब्जियों की खेती करने से साप्ताहिक नकद आमदनी बनी रहती है, जो बहुत लाभदायक है।""";
        };
    }

    // ── High-Profit Advisory ───────────────────────────────────────────────────

    private String getHighProfitAdvisory(String lang) {
        return switch (lang) {
            case "en" -> """
                    Hello farmer! Here are actionable tips to maximize farming profit:
                    
                    1. **High-Value Cash Crops**: Cultivate high-value crops like Pomegranate, Grapes, Dragon Fruit, Sugarcane, Cotton, or Spices instead of traditional grains only.
                    2. **Modern Irrigation**: Use Drip Irrigation. It saves 40% water, increases yield by 30%, and allows automated fertilizer application (Fertigation).
                    3. **Organic Farming**: Get Organic Certification. Certified organic vegetables and grains sell at 20-50% higher premium prices in cities.
                    4. **Protected Cultivation**: Use polyhouse/greenhouses for exotic vegetables (Capsicum, English Cucumber) to get off-season premium prices.
                    5. **Market Directly**: Register on e-NAM to sell directly to buyers nationwide, bypassing middlemen commissions.""";
            case "mr" -> """
                    नमस्कार शेतकरी बंधू! शेतीतून जास्तीत जास्त नफा मिळवण्यासाठी काही महत्त्वाचे मार्ग:
                    
                    1. **फायदेशीर नगदी पिके**: केवळ पारंपरिक धान्याऐवजी डाळिंब, द्राक्षे, ड्रॅगन फ्रूट, ऊस, कापूस किंवा मिरची व आल्यासारखी नगदी पिके घ्या.
                    2. **ठिबक सिंचन**: ठिबक सिंचन पद्धती वापरा. यामुळे ४०% पाणी वाचते, उत्पन्न ३०% वाढते आणि विद्राव्य खते पाण्यातून थेट देता येतात (Fertigation).
                    3. **सेंद्रिय शेती**: सेंद्रिय शेतीचे प्रमाणीकरण (Organic Certification) मिळवा. शहरात सेंद्रिय भाजीपाला व फळांना ३० ते ५० टक्के जास्त भाव मिळतो.
                    4. **पॉलीहाऊस शेती**: पॉलीहाऊसमध्ये रंगीत सिमला मिरची, ब्रोकोली किंवा काकडी यांसारख्या पिकांची लागवड करून अवकाळी हंगामात दुप्पट नफा मिळवा.
                    5. **थेट विक्री**: दलालांशिवाय थेट ग्राहकांना किंवा ई-नाम (e-NAM) द्वारे देशभरातील व्यापाऱ्यांना माल विकून कमिशनची बचत करा.""";
            case "te" -> """
                    నమస్కారం రైతు సోదరా! వ్వవసాయంలో అధిక లాభాలు పొందడానికి ముఖ్యమైన సలహాలు:
                    
                    1. **వాణిజ్య పంటలు**: కేవలం వరి, గోధుమలే కాకుండా దానిమ్మ, ద్రాక్ష, డ్రాగన్ ఫ్రూట్, చెరకు, పత్తి లేదా మిరప, అల్లం వంటి వాణిజ్య పంటలు వేయండి.
                    2. **డ్రిప్ నీటిపారుదల**: డ్రిప్ పద్ధతి వాడటం వల్ల 40% నీరు ఆదా అవుతుంది, 30% దిగుబడి పెరుగుతుంది. ఎరువులను నేరుగా అందించవచ్చు (Fertigation).
                    3. **சேంద్రీయ వ్యవసాయం**: ఆర్గానిక్ సర్టిఫికేషన్ పొందండి. నగరాల్లో సేంద్రీయ కూరగాయలు మరియు ధాన్యాలకు 20-50% ఎక్కువ ధర లభిస్తుంది.
                    4. **పాలీహౌస్ సాగు**: రంగుల క్యాప్సికం, కీర దోస వంటి విలువైన కూరగాయలను పాలీహౌస్ లో సాగు చేయడం ద్వారా అన్-సీజన్ లో అధిక లాభాలు పొందవచ్చు.
                    5. **డైరెక్ట్ మార్కెటింగ్**: దళారీల అవసరం లేకుండా ప్రభుత్వం ప్రవేశపెట్టిన ఇ-నామ్ (e-NAM) ద్వారా మీ పంటను దేశవ్యాప్తంగా నేరుగా అమ్ముకోవచ్చు.""";
            case "kn" -> """
                    ನಮಸ್ಕಾರ ರೈತ ಬಂಧು! ಕೃಷಿಯಲ್ಲಿ ಗರಿಷ್ಠ ಲಾಭ ಗಳಿಸಲು ಪ್ರಮುಖ ಮಾರ್ಗೋಪಾಯಗಳು:
                    
                    1. **ನಗದು ಬೆಳೆಗಳು**: ಕೇವಲ ಧಾನ್ಯಗಳ ಬದಲು ದಾಳಿಂಬೆ, ದ್ರಾಕ್ಷಿ, ಡ್ರ್ಯಾಗನ್ ಫ್ರೂಟ್, ಕಬ್ಬು, ಹತ್ತಿ ಅಥವಾ ಶುಂಠಿ, ಏಲಕ್ಕಿಯಂತಹ ನಗದು ಬೆಳೆಗಳನ್ನು ಬೆಳೆಯಿರಿ.
                    2. **ಹನಿ ನೀರಾವರಿ**: ಹನಿ ನೀರಾವರಿ ಪದ್ಧತಿ ಅಳವಡಿಸಿ. ಇದು 40% ನೀರನ್ನು ಉಳಿಸುತ್ತದೆ ಮತ್ತು 30% ಇಳುವರಿಯನ್ನು ಹೆಚ್ಚಿಸುತ್ತದೆ.
                    3. **ಸಾವಯವ ಕೃಷಿ**: ಸಾವಯವ ಪ್ರಮಾಣೀಕರಣ ಪಡೆಯಿರಿ. ಸಾವಯವ ತರಕಾರಿಗಳು ಮತ್ತು ಧಾನ್ಯಗಳಿಗೆ ಮಾರುಕಟ್ಟೆಯಲ್ಲಿ 20-50% ರಷ್ಟು ಹೆಚ್ಚಿನ ಬೆಲೆ ದೊರೆಯುತ್ತದೆ.
                    4. **ಹಸಿರುಮನೆ (Polyhouse)**: ಹಸಿರುಮನೆಯಲ್ಲಿ ಆಫ್-ಸೀಸನ್ ತರಕಾರಿಗಳನ್ನು (ಬಣ್ಣದ ಕ್ಯಾಪ್ಸಿಕಂ, ಸೌತೆಕಾಯಿ) ಬೆಳೆದು ಮಾರುಕಟ್ಟೆಯಲ್ಲಿ ಗರಿಷ್ಠ ಬೆಲೆ ಪಡೆಯಿರಿ.
                    5. **ನೇರ ಮಾರಾಟ**: ಮಧ್ಯವರ್ತಿಗಳ ಕಾಟ ತಪ್ಪಿಸಲು ಸರ್ಕಾರದ ಇ-ನಾಮ್ (e-NAM) ಪೋರ್ಟಲ್‌ನಲ್ಲಿ ನೋಂದಾಯಿಸಿಕೊಂಡು ಬೆಳೆಗಳನ್ನು ನೇರವಾಗಿ ಮಾರಿ ಹೆಚ್ಚಿನ ಲಾಭ ಗಳಿಸಿ.""";
            default -> """
                    नमस्ते किसान भाई! खेती से अधिकतम मुनाफा कमाने के प्रमुख उपाय:
                    
                    1. **नकदी फसलें**: केवल अनाज की जगह अधिक लाभ देने वाली नकदी फसलें जैसे अनार, अंगूर, गन्ना, कपास, अदरक, लहसुन या मसाले उगाएं।
                    2. **आधुनायिक सिंचाई**: ड्रिप (टपक) विधि का प्रयोग करें। इससे 40% पानी बचता है, पैदावार 30% बढ़ती है और खाद सीधे पानी के साथ (फर्टिगेशन) दी जा सकती है।
                    3. **जैविक खेती**: जैविक प्रमाणीकरण (Organic Certification) प्राप्त करें। शहरों में जैविक सब्जियों की कीमत 20-50% अधिक मिलती है।
                    4. **पॉलीहाउस**: बिना मौसम की सब्जियां (जैसे शिमला मिर्च, खीरा) उगाने के लिए पॉलीहाउस तकनीक अपनाएं, जिससे बाजार में दोगुनी कीमत मिलेगी।
                    5. **सीधी बिक्री**: बिचौलियों से बचने के लिए सरकारी ई-नाम (e-NAM) पोर्टल पर पंजीकरण कर अपनी उपज सीधे व्यापारियों को बेचें।""";
        };
    }

    // ── Multi-language Crop Advisory (Generic Fallback) ───────────────────────

    private String getCropAdvisory(String lang) {
        return switch (lang) {
            case "en" -> """
                    Hello farmer! Here is crop information for you:

                    1. Current season (Kharif) is the right time for sowing paddy, maize, soybean, and groundnut.
                    2. Always treat seeds before sowing — use Thiram or Carbendazim at 2g per kg of seeds.
                    3. Get soil tested and use appropriate fertilizers accordingly.
                    4. Ensure irrigation arrangement beforehand.
                    5. Get improved seeds from your nearest Krishi Vigyan Kendra (KVK).

                    Ask your question in detail for more specific guidance.""";
            case "mr" -> """
                    नमस्कार शेतकरी बंधू! तुमच्या पिकाबद्दल माहिती:

                    1. सध्याच्या हंगामात (खरीप) भात, मका, सोयाबीन आणि भुईमूग पेरणीची योग्य वेळ आहे.
                    2. पेरणीपूर्वी बीजप्रक्रिया अवश्य करा — थायरम किंवा कार्बेन्डाझिम 2 ग्रॅम प्रति किलो बियाणे.
                    3. मातीची तपासणी करून योग्य खतांचा वापर करा.
                    4. सिंचनाची व्यवस्था आधीच करा.
                    5. तुमच्या जवळच्या कृषी विज्ञान केंद्रातून (KVK) सुधारित बियाणे घ्या.

                    अधिक माहितीसाठी तुमचा प्रश्न विस्ताराने विचारा.""";
            case "te" -> """
                    నమస్కారం రైతు సోదరా! మీ పంట గురించి సమాచారం:

                    1. ప్రస్తుత సీజన్ (ఖరీఫ్) లో వరి, మొక్కజొన్న, సోయాబీన్, వేరుశనగ విత్తడానికి సరైన సమయం.
                    2. విత్తన శుద్ధి తప్పనిసరి — విత్తడానికి ముందు థైరమ్ లేదా కార్బెండజిమ్ ప్రతి కిలో విత్తనాలకు 2 గ్రా.
                    3. మట్టి పరీక్ష చేయించి సరైన ఎరువులు వాడండి.
                    4. నీటిపారుదల ఏర్పాటు ముందుగానే చేయండి.
                    5. సమీపంలోని కృషి విజ్ఞాన కేంద్రం (KVK) నుండి మెరుగైన విత్తనాలు పొందండి.

                    మరింత సమాచారం కోసం మీ ప్రశ్నను వివరంగా అడగండి.""";
            case "kn" -> """
                    ನಮಸ್ಕಾರ ರೈತ ಬಂಧು! ನಿಮ್ಮ ಬೆಳೆ ಕುರಿತು ಮಾಹಿತಿ:

                    1. ಪ್ರಸ್ತುತ ಋತು (ಖಾರಿಫ್) ನಲ್ಲಿ ಭತ್ತ, ಮೆಕ್ಕೆಜೋಳ, ಸೋಯಾಬೀನ್ ಮತ್ತು ಕಡಲೆಕಾಯಿ ಬಿತ್ತನೆಗೆ ಸರಿಯಾದ ಸಮಯ.
                    2. ಬಿತ್ತನೆಗೆ ಮೊದಲು ಬೀಜೋಪಚಾರ ಮಾಡಿ — ಥೈರಮ್ ಅಥವಾ ಕಾರ್ಬೆಂಡಜಿಮ್ ಪ್ರತಿ ಕೆ.ಜಿ. ಬೀಜಕ್ಕೆ 2 ಗ್ರಾಂ.
                    3. ಮಣ್ಣು ಪರೀಕ್ಷೆ ಮಾಡಿಸಿ ಸೂಕ್ತ ಗೊಬ್ಬರ ಬಳಸಿ.
                    4. ನೀರಾವರಿ ವ್ಯವಸ್ಥೆ ಮೊದಲೇ ಮಾಡಿ.
                    5. ಹತ್ತಿರದ ಕೃಷಿ ವಿಜ್ಞಾನ ಕೇಂದ್ರದಿಂದ (KVK) ಸುಧಾರಿತ ಬೀಜ ಪಡೆಯಿರಿ.

                    ಹೆಚ್ಚಿನ ಮಾಹಿತಿಗಾಗಿ ನಿಮ್ಮ ಪ್ರಶ್ನೆ ವಿವರವಾಗಿ ಕೇಳಿ.""";
            default -> """
                    नमस्ते किसान भाई! आपकी फसल के बारे में जानकारी:

                    1. वर्तमान मौसम (खरीफ) में धान, मक्का, सोयाबीन, और मूंगफली की बुआई का सही समय है।
                    2. बुआई से पहले बीज उपचार अवश्य करें — थीरम या कार्बेन्डाजिम 2 ग्राम प्रति किलो बीज की दर से उपचारित करें।
                    3. मिट्टी की जांच करवाकर उचित उर्वरक का प्रयोग करें।
                    4. सिंचाई की व्यवस्था पहले से सुनिश्चित करें।
                    5. अपने नजदीकी कृषि विज्ञान केंद्र (KVK) से उन्नत बीज प्राप्त करें।

                    अधिक जानकारी के लिए अपना सवाल विस्तार से पूछें।""";
        };
    }

    // ── Multi-language Weather Advisory ───────────────────────────────────────

    private String getWeatherAdvisory(String lang) {
        return switch (lang) {
            case "en" -> """
                    Hello farmer! Weather information:

                    1. Please share your farm location (district/village) for accurate weather updates.
                    2. General advice: Maintain proper drainage in fields during monsoon.
                    3. Complete harvesting early if heavy rain is expected.
                    4. During extreme heat, irrigate during early morning or evening only.
                    5. Ensure you have Pradhan Mantri Fasal Bima Yojana (PMFBY) insurance.

                    Share your district name and I can provide local weather forecasts.""";
            case "mr" -> """
                    नमस्कार शेतकरी बंधू! हवामानाची माहिती:

                    1. कृपया तुमच्या शेताचे ठिकाण (जिल्हा/गाव) सांगा म्हणजे अचूक हवामान माहिती देता येईल.
                    2. सामान्य सल्ला: पावसाळ्यात शेतातून पाण्याच्या निचऱ्याची व्यवस्था ठेवा.
                    3. जोरदार पावसाची शक्यता असल्यास पीक कापणी लवकर करा.
                    4. कडक उन्हात सिंचन सकाळी किंवा संध्याकाळी करा.
                    5. प्रधानमंत्री फसल विमा योजना (PMFBY) अवश्य काढा.

                    तुमच्या जिल्ह्याचे नाव सांगा, स्थानिक हवामान अंदाज देतो.""";
            case "te" -> """
                    నమస్కారం రైతు సోదరా! వాతావరణ సమాచారం:

                    1. దయచేసి మీ పొలం ప్రాంతం (జిల్లా/గ్రామం) చెప్పండి, ఖచ్చితమైన వాతావరణ సమాచారం ఇవ్వగలం.
                    2. సాధారణ సలహా: వర్షాకాలంలో పొలాల్లో నీటి నిర్వహణ ఏర్పాటు ఉంచండి.
                    3. భారీ వర్షం అవకాశం ఉంటే పంట కోత త్వరగా చేయండి.
                    4. ఎండ తీవ్రంగా ఉన్నప్పుడు ఉదయం లేదా సాయంత్రం మాత్రమే నీళ్ళు పెట్టండి.
                    5. ప్రధాన మంత్రి ఫసల్ బీమా యోజన (PMFBY) తప్పనిసరిగా తీసుకోండి.

                    మీ జిల్లా పేరు చెప్పండి, స్థానిక వాతావరణ అంచనా ఇస్తాను.""";
            case "kn" -> """
                    ನಮಸ್ಕಾರ ರೈತ ಬಂಧು! ಹವಾಮಾನ ಮಾಹಿತಿ:

                    1. ದಯವಿಟ್ಟು ನಿಮ್ಮ ಹೊಲದ ಸ್ಥಳ (ಜಿಲ್ಲೆ/ಹಳ್ಳಿ) ತಿಳಿಸಿ, ನಿಖರ ಹವಾಮಾನ ಮಾಹಿತಿ ನೀಡಬಹುದು.
                    2. ಸಾಮಾನ್ಯ ಸಲಹೆ: ಮಳೆಗಾಲದಲ್ಲಿ ಹೊಲಗಳಲ್ಲಿ ಒಳಚರಂಡಿ ವ್ಯವಸ್ಥೆ ಇರಲಿ.
                    3. ಭಾರಿ ಮಳೆ ಸಾಧ್ಯತೆ ಇದ್ದರೆ ಬೆಳೆ ಕೊಯ್ಲು ಬೇಗ ಮಾಡಿ.
                    4. ತೀವ್ರ ಬಿಸಿಲಿನಲ್ಲಿ ಬೆಳಿಗ್ಗೆ ಅಥವಾ ಸಂಜೆ ಮಾತ್ರ ನೀರು ಹಾಯಿಸಿ.
                    5. ಪ್ರಧಾನ ಮಂತ್ರಿ ಫಸಲ್ ಬೀಮಾ ಯೋಜನೆ (PMFBY) ಖಂಡಿತ ಮಾಡಿಸಿ.

                    ನಿಮ್ಮ ಜಿಲ್ಲೆ ಹೆಸರು ಹೇಳಿ, ಸ್ಥಳೀಯ ಹವಾಮಾನ ಮುನ್ಸೂಚನೆ ಕಳಿಸುತ್ತೇನೆ.""";
            default -> """
                    नमस्ते किसान भाई! मौसम की जानकारी:

                    1. कृपया अपने खेत का स्थान (ज़िला/गांव) बताएं ताकि सटीक मौसम की जानकारी दी जा सके।
                    2. सामान्य सलाह: मानसून के दौरान खेतों में जल निकासी की व्यवस्था रखें।
                    3. भारी बारिश की संभावना होने पर फसल कटाई जल्दी करें।
                    4. तेज धूप में सिंचाई सुबह या शाम को करें।
                    5. मौसम आधारित फसल बीमा (PMFBY) अवश्य कराएं।

                    अपने ज़िले का नाम बताएं तो स्थानीय मौसम पूर्वानुमान भेज सकते हैं।""";
        };
    }

    // ── Multi-language Disease Advisory ───────────────────────────────────────

    private String getDiseaseAdvisory(String lang) {
        return switch (lang) {
            case "en" -> """
                    Hello farmer! Crop disease assistance:

                    1. For accurate disease identification, please upload a photo of the affected leaf or plant.
                    2. Go to the 'Disease Detection' section to upload photos.
                    3. General suggestions:
                       - Remove affected leaves immediately and burn them.
                       - Spray neem oil (5 ml per litre of water).
                       - Practice crop rotation — don't grow the same crop repeatedly.
                    4. Use pesticides only on advice from an agriculture officer.

                    Send a photo and we will use AI to identify the disease and suggest accurate treatment!""";
            case "mr" -> """
                    नमस्कार शेतकरी बंधू! पीक रोग सहाय्य:

                    1. रोगाची अचूक ओळख करण्यासाठी कृपया प्रभावित पान किंवा रोपाचा फोटो अपलोड करा.
                    2. फोटो अपलोड करण्यासाठी 'रोग ओळख' विभागात जा.
                    3. सामान्य सूचना:
                       - प्रभावित पाने ताबडतोब काढून जाळून टाका.
                       - कडुनिंबाचे तेल (5 मिली प्रति लिटर पाणी) फवारणी करा.
                       - पीक फेरपालट करा — एकच पीक पुन्हा पुन्हा लावू नका.
                    4. कीटकनाशकांचा वापर कृषी अधिकाऱ्यांच्या सल्ल्यानेच करा.

                    फोटो पाठवा, आम्ही AI ने रोग ओळखून अचूक उपचार सांगू!""";
            case "te" -> """
                    నమస్కారం రైతు సోదరా! పంట వ్యాధి సహాయం:

                    1. వ్యాధిని సరిగ్గా గుర్తించడానికి దయచేసి ప్రభావిత ఆకు లేదా మొక్క ఫోటో అప్‌లోడ్ చేయండి.
                    2. ఫోటో అప్‌లోడ్ చేయడానికి 'వ్యాధి గుర్తింపు' విభాగానికి వెళ్ళండి.
                    3. సాధారణ సూచనలు:
                       - ప్రభావిత ఆకులను వెంటనే తీసి కాల్చివేయండి.
                       - వేప నూనె (లీటరు నీటికి 5 మి.లీ.) పిచికారీ చేయండి.
                       - పంట మార్పిడి చేయండి — ఒకే పంట మళ్ళీ మళ్ళీ వేయకండి.
                    4. పురుగుమందులు వ్యవసాయ అధికారి సలహాపై మాత్రమే వాడండి.

                    ఫోటో పంపండి, AI తో వ్యాధి గుర్తించి సరైన చికిత్స చెప్తాము!""";
            case "kn" -> """
                    ನಮಸ್ಕಾರ ರೈತ ಬಂಧು! ಬೆಳೆ ರೋಗ ಸಹಾಯ:

                    1. ರೋಗದ ನಿಖರ ಗುರುತಿಸುವಿಕೆಗೆ ದಯವಿಟ್ಟು ಬಾಧಿತ ಎಲೆ ಅಥವಾ ಗಿಡದ ಫೋಟೋ ಅಪ್‌ಲೋಡ್ ಮಾಡಿ.
                    2. ಫೋಟೋ ಅಪ್‌ಲೋಡ್ ಮಾಡಲು 'ರೋಗ ಪತ್ತೆ' ವಿಭಾಗಕ್ಕೆ ಹೋಗಿ.
                    3. ಸಾಮಾನ್ಯ ಸಲಹೆಗಳು:
                       - ಬಾಧಿತ ಎಲೆಗಳನ್ನು ತಕ್ಷಣ ತೆಗೆದು ಸುಡಿ.
                       - ಬೇವಿನ ಎಣ್ಣೆ (ಲೀಟರ್ ನೀರಿಗೆ 5 ಮಿ.ಲೀ.) ಸಿಂಪಡಿಸಿ.
                       - ಬೆಳೆ ಸರದಿ ಅನುಸರಿಸಿ — ಒಂದೇ ಬೆಳೆಯನ್ನು ಮತ್ತೆ ಮತ್ತೆ ಬೆಳೆಯಬೇಡಿ.
                    4. ಕೀಟನಾಶಕ ಕೃಷಿ ಅಧಿಕಾರಿ ಸಲಹೆ ಮೇಲೆ ಮಾತ್ರ ಬಳಸಿ.

                    ಫೋಟೋ ಕಳಿಸಿ, AI ಮೂಲಕ ರೋಗ ಗುರುತಿಸಿ ನಿಖರ ಚಿಕಿತ್ಸೆ ಹೇಳುತ್ತೇವೆ!""";
            default -> """
                    नमस्ते किसान भाई! फसल रोग सहायता:

                    1. रोग की सही पहचान के लिए कृपया प्रभावित पत्ते या पौधे की फोटो अपलोड करें।
                    2. फोटो अपलोड करने के लिए 'रोग पहचान' अनुभाग में जाएं।
                    3. सामान्य सुझाव:
                       - प्रभावित पत्तों को तुरंत हटाएं और जला दें।
                       - नीम तेल (5 मिली/लीटर पानी) का छिड़काव करें।
                       - फसल चक्र अपनाएं — एक ही फसल बार-बार न उगाएं।
                    4. कीटनाशक का प्रयोग कृषि अधिकारी की सलाह पर ही करें।

                    फोटो भेजें, हम AI से रोग की पहचान कर सटीक उपचार बताएंगे!""";
        };
    }

    // ── Multi-language Scheme Advisory ────────────────────────────────────────

    private String getSchemeAdvisory(String lang) {
        return switch (lang) {
            case "en" -> """
                    Hello farmer! Government scheme information:

                    Key Schemes:
                    1. PM-Kisan: Rs 6,000 annually (in 3 installments) — for all farmers.
                    2. PM Fasal Bima Yojana (PMFBY): Crop insurance against natural disasters.
                    3. Kisan Credit Card (KCC): Low-interest agricultural loans.
                    4. Soil Health Card: Free soil testing and fertilizer recommendations.
                    5. e-NAM: Online marketplace — sell your produce directly.

                    Check the 'Scheme Recommendations' section for personalized scheme suggestions based on your profile.""";
            case "mr" -> """
                    नमस्कार शेतकरी बंधू! सरकारी योजनांची माहिती:

                    प्रमुख योजना:
                    1. पीएम-किसान: वार्षिक ₹6,000 (3 हप्त्यांमध्ये) — सर्व शेतकऱ्यांसाठी.
                    2. पीएम फसल विमा योजना (PMFBY): नैसर्गिक आपत्तीमुळे पीक नुकसानीवर विमा.
                    3. किसान क्रेडिट कार्ड (KCC): कमी व्याजदरावर कृषी कर्ज.
                    4. सॉइल हेल्थ कार्ड: मोफत माती तपासणी आणि खत शिफारस.
                    5. ई-नाम (e-NAM): ऑनलाइन बाजार — तुमचे उत्पादन थेट विका.

                    तुमच्या प्रोफाइलनुसार वैयक्तिक शिफारशींसाठी 'योजना शिफारस' विभाग पहा.""";
            case "te" -> """
                    నమస్కారం రైతు సోదరా! ప్రభుత్వ పథకాల సమాచారం:

                    ప్రధాన పథకాలు:
                    1. PM-కిసాన్: సంవత్సరానికి ₹6,000 (3 వాయిదాల్లో) — అన్ని రైతులకు.
                    2. PM ఫసల్ బీమా యోజన (PMFBY): ప్రకృతి విపత్తుల వల్ల పంట నష్టానికి బీమా.
                    3. కిసాన్ క్రెడిట్ కార్డ్ (KCC): తక్కువ వడ్డీ రేటుతో వ్యవసాయ రుణాలు.
                    4. సాయిల్ హెల్త్ కార్డ్: ఉచిత మట్టి పరీక్ష మరియు ఎరువుల సిఫార్సులు.
                    5. ఈ-నామ్ (e-NAM): ఆన్‌లైన్ మార్కెట్ — మీ ఉత్పత్తులను నేరుగా అమ్మండి.

                    మీ ప్రొఫైల్ ఆధారంగా వ్యక్తిగత సిఫార్సుల కోసం 'పథక సిఫార్సులు' విభాగం చూడండి.""";
            case "kn" -> """
                    ನಮಸ್ಕಾರ ರೈತ ಬಂಧು! ಸರ್ಕಾರಿ ಯೋಜನೆಗಳ ಮಾಹಿತಿ:

                    ಪ್ರಮುಖ ಯೋಜನೆಗಳು:
                    1. PM-ಕಿಸಾನ್: ವಾರ್ಷಿಕ ₹6,000 (3 ಕಂತುಗಳಲ್ಲಿ) — ಎಲ್ಲಾ ರೈತರಿಗೆ.
                    2. PM ಫಸಲ್ ಬೀಮಾ ಯೋಜನೆ (PMFBY): ನೈಸರ್ಗಿಕ ವಿಪತ್ತಿನಿಂದ ಬೆಳೆ ನಷ್ಟಕ್ಕೆ ವಿಮೆ.
                    3. ಕಿಸಾನ್ ಕ್ರೆಡಿಟ್ ಕಾರ್ಡ್ (KCC): ಕಡಿಮೆ ಬಡ್ಡಿದರದಲ್ಲಿ ಕೃಷಿ ಸಾಲ.
                    4. ಸಾಯಿಲ್ ಹೆಲ್ತ್ ಕಾರ್ಡ್: ಉಚಿತ ಮಣ್ಣು ಪರೀಕ್ಷೆ ಮತ್ತು ಗೊಬ್ಬರ ಶಿಫಾರಸು.
                    5. ಈ-ನಾಮ್ (e-NAM): ಆನ್‌ಲೈನ್ ಮಾರುಕಟ್ಟೆ — ನಿಮ್ಮ ಉತ್ಪನ್ನ ನೇರವಾಗಿ ಮಾರಿ.

                    ನಿಮ್ಮ ಪ್ರೊಫೈಲ್ ಆಧಾರದಲ್ಲಿ ವೈಯक्तಿಕ ಶಿಫಾರಸುಗಳಿಗೆ 'ಯೋಜನೆ ಶಿಫಾರಸುಗಳು' ವಿಭಾಗ ನೋಡಿ.""";
            default -> """
                    नमस्ते किसान भाई! सरकारी योजनाओं की जानकारी:

                    प्रमुख योजनाएं:
                    1. पीएम-किसान: ₹6,000 वार्षिक (3 किस्तों में) — सभी किसानों के लिए।
                    2. पीएम फसल बीमा योजना (PMFBY): प्राकृतिक आपदा से फसल नुकसान पर बीमा।
                    3. किसान क्रेडिट कार्ड (KCC): कम ब्याज दर पर कृषि ऋण।
                    4. सॉइल हेल्थ कार्ड: मुफ्त मिट्टी जांच और उर्वरक सिफारिश।
                    5. ई-नाम (e-NAM): ऑनलाइन मंडी — अपनी उपज सीधे बेचें।

                    व्यक्तिगत सिफारिश के लिए 'योजना सिफारिश' अनुभाग देखें जहां आपकी प्रोफाइल के अनुसार योजनाएं सुझाई जाती हैं।""";
        };
    }

    // ── Multi-language General Advisory ───────────────────────────────────────

    private String getGeneralAdvisory(String lang) {
        return switch (lang) {
            case "en" -> """
                    Hello farmer! Welcome to KrishiMitra AI!

                    I can help you with these topics:

                    🌾 Crop Advice — sowing, irrigation, fertilizer guidance
                    🌤️ Weather — weather forecasts and farming advice
                    🐛 Disease Detection — identify crop diseases from photos
                    📋 Government Schemes — scheme info and applications

                    Ask your question in Hindi or English. You can also speak your question!

                    Example: "When should I sow wheat?" or "Tell me about PM Kisan scheme\"""";
            case "mr" -> """
                    नमस्कार शेतकरी बंधू! कृषिमित्र AI मध्ये आपले स्वागत आहे!

                    मी तुम्हाला या विषयांवर मदत करू शकतो:

                    🌾 पीक सल्ला — पेरणी, सिंचन, खत मार्गदर्शन
                    🌤️ हवामान — हवामान अंदाज आणि सल्ला
                    🐛 रोग ओळख — फोटोवरून पीक रोग ओळखा
                    📋 सरकारी योजना — योजनांची माहिती आणि अर्ज

                    तुमचा प्रश्न मराठी, हिंदी किंवा इंग्रजीत विचारा. तुम्ही बोलूनही प्रश्न विचारू शकता!

                    उदाहरण: "गहू पेरणी कधी करावी?" किंवा "PM किसान योजनेबद्दल सांगा\"""";
            case "te" -> """
                    నమస్కారం రైతు సోదరా! కృషిమిత్ర AI కి స్వాగతం!

                    నేను ఈ అంశాలలో మీకు సహాయపడగలను:

                    🌾 పంట సలహా — విత్తడం, నీటిపారుదల, ఎరువుల మార్గదర్శకత్వం
                    🌤️ వాతావరణం — వాతావరణ అంచనాలు మరియు సలహా
                    🐛 వ్యాధి గుర్తింపు — ఫోటోల నుండి పంట వ్యాధులను గుర్తించండి
                    📋 ప్రభుత్వ పథకాలు — పథక సమాచారం మరియు దరఖాస్తులు

                    మీ ప్రశ్నను తెలుగు, హిందీ లేదా ఆంగ్లంలో అడగండి. మీరు మాట్లాడి भी प्रश्न अड़గవచ్చు!

                    ఉదాహరణ: "గోధుమ ఎప్పుడు విత్తాలి?" లేదా "PM కిసాన్ పథకం గురించి చెప్పండి\"""";
            case "kn" -> """
                    ನಮಸ್ಕಾರ ರೈತ ಬಂಧು! ಕೃಷಿಮಿತ್ರ AI ಗೆ ಸ್ವಾಗತ!

                    ನಾನು ಈ ವಿಷಯಗಳಲ್ಲಿ ನಿಮಗೆ ಸಹಾಯ ಮಾಡಬಲ್ಲೆ:

                    🌾 ಬೆಳೆ ಸಲಹೆ — ಬಿತ್ತನೆ, ನೀರಾವರಿ, ಗೊಬ್ಬರ ಮಾರ್ಗದರ್ಶನ
                    🌤️ ಹವಾಮಾನ — ಹವಾಮಾನ ಮುನ್ಸೂಚನೆ ಮತ್ತು ಸಲಹೆ
                    🐛 ರೋಗ ಪತ್ತೆ — ಫೋಟೋಗಳಿಂದ ಬೆಳೆ ರೋಗಗಳನ್ನು ಗುರುತಿಸಿ
                    📋 ಸರ್ಕಾರಿ ಯೋಜನೆಗಳು — ಯೋಜನೆ ಮಾಹಿತಿ ಮತ್ತು ಅರ್ಜಿ

                    ನಿಮ್ಮ ಪ್ರಶ್ನೆ ಕನ್ನಡ, ಹಿಂದಿ ಅಥವಾ ಇಂಗ್ಲಿಷ್‌ನಲ್ಲಿ ಕೇಳಿ. ಮಾತನಾಡಿಯೂ ಪ್ರಶ್ನೆ ಕೇಳಬಹುದು!

                    ಉದಾಹರಣೆ: "ಗೋಧಿ ಬಿತ್ತನೆ ಯಾವಾಗ ಮಾಡಬೇಕು?" ಅಥವಾ "PM ಕಿಸಾನ್ ಯೋಜನೆ ಬಗ್ಗೆ ಹೇಳಿ\"""";
            default -> """
                    नमस्ते किसान भाई! कृषिमित्र AI में आपका स्वागत है! 🙏

                    मैं आपकी इन विषयों में मदद कर सकता हूं:

                    🌾 फसल सलाह — बुआई, सिंचाई, उर्वरक की जानकारी
                    🌤️ मौसम — मौसम पूर्वानुमान और सलाह
                    🐛 रोग पहचान — फोटो से फसल रोग की पहचान
                    📋 सरकारी योजनाएं — योजनाओं की जानकारी और आवेदन

                    अपना सवाल हिंदी या अंग्रेजी में पूछें। आप बोलकर भी सवाल पूछ सकते हैं!

                    उदाहरण: "गेहूं की बुआई कब करें?" या "PM Kisan योजना के बारे में बताएं\"""";
        };
    }
}
