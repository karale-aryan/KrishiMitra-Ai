package com.krishimitra.croprecommendation.internal;

import ai.onnxruntime.OnnxTensor;
import ai.onnxruntime.OrtEnvironment;
import ai.onnxruntime.OrtException;
import ai.onnxruntime.OrtSession;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.*;
import java.util.stream.Collectors;

/**
 * ONNX Runtime service for crop recommendation inference.
 * Loads a pre-trained model that predicts suitable crops based on soil and weather features.
 * Falls back to mock predictions in dev environments where the model file is absent.
 */
@Slf4j
@Service
public class CropOnnxModelService {

    @Value("${krishimitra.ml.crop-model-path:models/crop_recommendation.onnx}")
    private String modelPath;

    private OrtEnvironment environment;
    private OrtSession session;
    private boolean modelLoaded = false;

    /**
     * The 22 crop labels corresponding to output indices of the trained model.
     * These match the standard Kaggle Crop Recommendation Dataset classes.
     */
    private static final String[] CROP_LABELS = {
            "rice", "maize", "chickpea", "kidneybeans", "pigeonpeas",
            "mothbeans", "mungbean", "blackgram", "lentil", "pomegranate",
            "banana", "mango", "grapes", "watermelon", "muskmelon",
            "apple", "orange", "papaya", "coconut", "cotton",
            "jute", "coffee"
    };

    @PostConstruct
    public void init() {
        try {
            File modelFile = new File(modelPath);
            if (!modelFile.exists()) {
                log.warn("Crop recommendation ONNX model not found at '{}'. Using mock predictions for development.", modelPath);
                return;
            }

            environment = OrtEnvironment.getEnvironment();
            session = environment.createSession(modelPath, new OrtSession.SessionOptions());
            modelLoaded = true;
            log.info("Crop recommendation ONNX model loaded successfully from '{}'", modelPath);
        } catch (OrtException e) {
            log.error("Failed to load crop recommendation ONNX model: {}", e.getMessage(), e);
        }
    }

    @PreDestroy
    public void cleanup() {
        try {
            if (session != null) {
                session.close();
            }
        } catch (OrtException e) {
            log.warn("Error closing ONNX session: {}", e.getMessage());
        }
    }

    /**
     * Predict crop suitability from soil and weather features.
     *
     * @param features array of [N, P, K, temperature, humidity, pH, rainfall]
     * @return map of crop name to confidence score, top 5 results sorted by confidence descending
     */
    public Map<String, Float> predict(float[] features) {
        if (!modelLoaded) {
            log.debug("Model not loaded, returning mock predictions");
            return getMockPredictions(features);
        }

        try {
            float[][] inputData = new float[1][7];
            System.arraycopy(features, 0, inputData[0], 0, Math.min(features.length, 7));

            OnnxTensor inputTensor = OnnxTensor.createTensor(environment, inputData);
            Map<String, OnnxTensor> inputs = Collections.singletonMap("float_input", inputTensor);

            OrtSession.Result result = session.run(inputs);

            // The model outputs probabilities for each class
            float[][] outputProbs;
            Object outputValue = result.get(0).getValue();
            if (outputValue instanceof float[][]) {
                outputProbs = (float[][]) outputValue;
            } else if (outputValue instanceof long[][]) {
                // Some models output class labels; handle gracefully
                long[][] labels = (long[][]) outputValue;
                outputProbs = new float[1][CROP_LABELS.length];
                int idx = (int) labels[0][0];
                if (idx >= 0 && idx < CROP_LABELS.length) {
                    outputProbs[0][idx] = 1.0f;
                }
            } else {
                log.warn("Unexpected ONNX output type: {}. Falling back to mock predictions.", outputValue.getClass().getName());
                inputTensor.close();
                result.close();
                return getMockPredictions(features);
            }

            Map<String, Float> predictions = new HashMap<>();
            for (int i = 0; i < Math.min(outputProbs[0].length, CROP_LABELS.length); i++) {
                predictions.put(CROP_LABELS[i], outputProbs[0][i]);
            }

            inputTensor.close();
            result.close();

            return getTopN(predictions, 5);
        } catch (OrtException e) {
            log.error("ONNX inference error: {}", e.getMessage(), e);
            return getMockPredictions(features);
        }
    }

    /**
     * Returns mock predictions based on feature heuristics for development without a model.
     */
    private Map<String, Float> getMockPredictions(float[] features) {
        float temperature = features.length > 3 ? features[3] : 25.0f;
        float humidity = features.length > 4 ? features[4] : 70.0f;
        float rainfall = features.length > 6 ? features[6] : 200.0f;

        Map<String, Float> mock = new LinkedHashMap<>();

        if (temperature > 30 && humidity > 70 && rainfall > 200) {
            mock.put("rice", 0.92f);
            mock.put("jute", 0.78f);
            mock.put("papaya", 0.65f);
            mock.put("coconut", 0.58f);
            mock.put("banana", 0.52f);
        } else if (temperature < 20 && humidity < 60) {
            mock.put("lentil", 0.88f);
            mock.put("chickpea", 0.82f);
            mock.put("kidneybeans", 0.71f);
            mock.put("mothbeans", 0.55f);
            mock.put("blackgram", 0.48f);
        } else if (temperature > 25 && rainfall < 100) {
            mock.put("cotton", 0.85f);
            mock.put("mungbean", 0.74f);
            mock.put("pigeonpeas", 0.68f);
            mock.put("mothbeans", 0.60f);
            mock.put("maize", 0.53f);
        } else {
            mock.put("maize", 0.82f);
            mock.put("rice", 0.76f);
            mock.put("banana", 0.68f);
            mock.put("mango", 0.61f);
            mock.put("grapes", 0.54f);
        }

        return mock;
    }

    /**
     * Extract top-N entries sorted by confidence descending.
     */
    private Map<String, Float> getTopN(Map<String, Float> predictions, int n) {
        return predictions.entrySet().stream()
                .sorted(Map.Entry.<String, Float>comparingByValue().reversed())
                .limit(n)
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (e1, e2) -> e1,
                        LinkedHashMap::new
                ));
    }
}
