package com.krishimitra.diseasedetection.internal;

import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class TreatmentKnowledgeBase {

    public record TreatmentInfo(
            String diseaseName,
            String diseaseNameHi,
            String description,
            String organicTreatment,
            String chemicalTreatment,
            String prevention,
            DiseaseReportEntity.Severity severity
    ) {}

    private final Map<String, TreatmentInfo> knowledgeBase = new HashMap<>();

    public TreatmentKnowledgeBase() {
        // Initialize the 38 PlantVillage classes
        
        // 1. Apple Scab
        knowledgeBase.put("Apple___Apple_scab", new TreatmentInfo(
                "Apple Scab",
                "सेब का स्कैब",
                "Fungal disease causing dark olive-green spots on leaves and fruit.",
                "Apply sulfur-based organic fungicides or neem oil early in the season.",
                "Use chemical fungicides containing copper oxychloride, captan, or mancozeb.",
                "Rake and destroy fallen leaves in autumn. Prune trees to improve air circulation.",
                DiseaseReportEntity.Severity.MODERATE
        ));

        // 2. Apple Cedar Rust
        knowledgeBase.put("Apple___Cedar_apple_rust", new TreatmentInfo(
                "Cedar Apple Rust",
                "सेब का देवदार जंग रोग",
                "Fungal disease causing bright orange-yellow spots on leaves. Requires cedar trees as alternate host.",
                "Spray copper-based organic sprays or pruning infected twigs on nearby junipers.",
                "Apply systemic fungicides like myclobutanil or triadimefon.",
                "Remove nearby red cedars if possible. Plant rust-resistant apple cultivars.",
                DiseaseReportEntity.Severity.MODERATE
        ));

        // 3. Apple Healthy
        knowledgeBase.put("Apple___healthy", new TreatmentInfo(
                "Healthy Apple Leaf",
                "स्वस्थ सेब का पत्ता",
                "No disease detected. Leaf is in healthy state.",
                "Maintain standard watering and organic composting.",
                "No chemical treatment required.",
                "Continue balanced crop nutrition and routine inspection.",
                DiseaseReportEntity.Severity.LOW
        ));

        // 4. Corn Common Rust
        knowledgeBase.put("Corn_(maize)___Common_rust_", new TreatmentInfo(
                "Corn Common Rust",
                "मक्के का सामान्य रतुआ",
                "Fungal disease producing golden-brown pustules on both upper and lower leaf surfaces.",
                "Use compost teas or apply sulfur dust to early stage crops.",
                "Spray fungicides like azoxystrobin, pyraclostrobin, or mancozeb.",
                "Plant rust-resistant corn varieties. Rotate crops yearly.",
                DiseaseReportEntity.Severity.MODERATE
        ));

        // 5. Corn Healthy
        knowledgeBase.put("Corn_(maize)___healthy", new TreatmentInfo(
                "Healthy Corn",
                "स्वस्थ मक्का",
                "No disease detected. Plant is in healthy state.",
                "Maintain optimal soil moisture and organic fertilizers.",
                "No chemical treatment required.",
                "Rotate crops with legumes. Ensure proper nitrogen management.",
                DiseaseReportEntity.Severity.LOW
        ));

        // 6. Grape Black Rot
        knowledgeBase.put("Grape___Black_rot", new TreatmentInfo(
                "Grape Black Rot",
                "अंगूर का काला सड़न रोग",
                "Severe fungal disease causing reddish-brown leaf spots and mummifying grape berries.",
                "Remove and burn all infected canes and mummified berries. Apply copper sprays.",
                "Spray chemical fungicides like mancozeb, ziram, or myclobutanil.",
                "Ensure proper trellis system for maximum sunlight exposure and wind flow.",
                DiseaseReportEntity.Severity.SEVERE
        ));

        // 7. Potato Early Blight
        knowledgeBase.put("Potato___Early_blight", new TreatmentInfo(
                "Potato Early Blight",
                "आलू का अगेती झुलसा",
                "Fungal disease causing dark, concentric 'target' spots on older leaves first.",
                "Apply copper-based organic sprays or horse tail decoction.",
                "Spray fungicides such as chlorothalonil, mancozeb, or azoxystrobin.",
                "Avoid overhead irrigation. Ensure crop rotation with non-solanaceous crops.",
                DiseaseReportEntity.Severity.MODERATE
        ));

        // 8. Potato Late Blight
        knowledgeBase.put("Potato___Late_blight", new TreatmentInfo(
                "Potato Late Blight",
                "आलू का पछेती झुलसा",
                "Highly destructive fungal-like disease causing water-soaked spots, white mold growth, and rapid rotting.",
                "Spray copper sulfate or copper hydroxide organic preparations immediately.",
                "Apply systemic fungicides like metalaxyl, mancozeb, or chlorothalonil.",
                "Use certified disease-free seed tubers. Destroy volunteer potato plants.",
                DiseaseReportEntity.Severity.CRITICAL
        ));

        // 9. Potato Healthy
        knowledgeBase.put("Potato___healthy", new TreatmentInfo(
                "Healthy Potato",
                "स्वस्थ आलू",
                "No disease detected. Foliage is in healthy state.",
                "Apply well-rotted manure and balanced watering.",
                "No chemical treatment required.",
                "Keep soil well-drained. Inspect leaves regularly.",
                DiseaseReportEntity.Severity.LOW
        ));

        // 10. Tomato Bacterial Spot
        knowledgeBase.put("Tomato___Bacterial_spot", new TreatmentInfo(
                "Tomato Bacterial Spot",
                "टमाटर का जीवाणु धब्बा रोग",
                "Bacterial disease causing dark, water-soaked spots on leaves, stems, and fruit.",
                "Apply copper-based sprays combined with organic compost extract.",
                "Use copper fungicides mixed with mancozeb to manage bacterial spread.",
                "Avoid overhead watering; use drip irrigation. Ensure tool sanitization.",
                DiseaseReportEntity.Severity.SEVERE
        ));

        // 11. Tomato Early Blight
        knowledgeBase.put("Tomato___Early_blight", new TreatmentInfo(
                "Tomato Early Blight",
                "टमाटर का अगेती झुलसा",
                "Fungal disease producing brown concentric circles on leaves, leading to leaf yellowing.",
                "Prune lower leaves to prevent soil splash. Spray copper fungicides.",
                "Apply chemical fungicides like chlorothalonil, difenoconazole, or mancozeb.",
                "Mulch around plants to block soil-borne spores. Rotate crops.",
                DiseaseReportEntity.Severity.MODERATE
        ));

        // 12. Tomato Late Blight
        knowledgeBase.put("Tomato___Late_blight", new TreatmentInfo(
                "Tomato Late Blight",
                "टमाटर का पछेती झुलसा",
                "Rapidly spreading disease under cool, wet conditions, causing large grey-brown spots and fuzzy white mold.",
                "Spray copper fungicides at the first sign of wet, humid weather.",
                "Apply systemic chemical fungicides like metalaxyl, cyazofamid, or chlorothalonil.",
                "Destroy infected crop residues immediately. Avoid planting near potatoes.",
                DiseaseReportEntity.Severity.CRITICAL
        ));

        // 13. Tomato Leaf Mold
        knowledgeBase.put("Tomato___Leaf_Mold", new TreatmentInfo(
                "Tomato Leaf Mold",
                "टमाटर का पत्ता मोल्ड रोग",
                "Fungal disease causing pale green/yellow spots on leaf upper surfaces and olive-purple velvety growth underneath.",
                "Increase greenhouse ventilation. Spray sulfur or copper-based sprays.",
                "Apply chemical fungicides like chlorothalonil or mancozeb.",
                "Keep humidity low. Space plants out to improve airflow.",
                DiseaseReportEntity.Severity.MODERATE
        ));

        // 14. Tomato Septoria Leaf Spot
        knowledgeBase.put("Tomato___Septoria_leaf_spot", new TreatmentInfo(
                "Tomato Septoria Leaf Spot",
                "टमाटर का सेप्टोरिया पत्ता धब्बा",
                "Fungal disease causing small circular spots with grey centers and dark borders on lower leaves.",
                "Remove infected lower leaves. Spray organic copper solution.",
                "Apply chemical fungicides containing chlorothalonil or mancozeb.",
                "Mulch soil and water at the base of the plant. Rotate crops.",
                DiseaseReportEntity.Severity.MODERATE
        ));

        // Generic fallback for all other classes to complete the 38 classes
        knowledgeBase.put("Tomato___healthy", new TreatmentInfo(
                "Healthy Tomato", "स्वस्थ टमाटर", "Tomato plant is healthy.", "Standard organic farming.", "None", "Regular inspection.", DiseaseReportEntity.Severity.LOW
        ));

        String[] genericClasses = {
                "Blueberry___healthy", "Cherry_(including_sour)___Powdery_mildew", "Cherry_(including_sour)___healthy",
                "Corn_(maize)___Cercospora_leaf_spot Gray_leaf_spot", "Corn_(maize)___Northern_Leaf_Blight",
                "Grape___Esca_(Black_Measles)", "Grape___Leaf_blight_(Isariopsis_Leaf_Spot)", "Grape___healthy",
                "Orange___Haunglongbing_(Citrus_greening)", "Peach___Bacterial_spot", "Peach___healthy",
                "Pepper,_bell___Bacterial_spot", "Pepper,_bell___healthy", "Raspberry___healthy", "Soybean___healthy",
                "Squash___Powdery_mildew", "Strawberry___Leaf_scorch", "Strawberry___healthy",
                "Tomato___Spider_mites Two-spotted_spider_mite", "Tomato___Target_Spot",
                "Tomato___Tomato_Yellow_Leaf_Curl_Virus", "Tomato___Tomato_mosaic_virus"
        };

        for (String c : genericClasses) {
            String cleanName = c.replace("___", " ").replace("_", " ");
            boolean isHealthy = c.toLowerCase().contains("healthy");
            knowledgeBase.put(c, new TreatmentInfo(
                    cleanName,
                    cleanName + " (हिंदी अनुवाद)",
                    "Plant disease or condition under observation: " + cleanName,
                    isHealthy ? "Maintain organic compost." : "Apply organic copper or sulfur spray.",
                    isHealthy ? "No chemical needed." : "Apply generic fungicide or pesticide.",
                    "Ensure crop rotation, weed control, and balanced irrigation.",
                    isHealthy ? DiseaseReportEntity.Severity.LOW : DiseaseReportEntity.Severity.MODERATE
            ));
        }
    }

    public TreatmentInfo getTreatment(String diseaseClass) {
        return knowledgeBase.getOrDefault(diseaseClass, new TreatmentInfo(
                "Unknown Disease",
                "अज्ञात बीमारी",
                "Unidentified plant disease class.",
                "Apply organic bio-fungicide or neem oil spray.",
                "Consult local agricultural extension officer.",
                "Inspect seeds and soil health regularly.",
                DiseaseReportEntity.Severity.MODERATE
        ));
    }
}
