package com.krishimitra.diseasedetection;

import com.krishimitra.diseasedetection.dto.DiseaseDetectionResponse;
import com.krishimitra.diseasedetection.internal.*;
import com.krishimitra.shared.exception.BadRequestException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class DiseaseDetectionService {

    private final DiseaseReportRepository diseaseReportRepository;
    private final DiseaseOnnxModelService onnxModelService;
    private final TreatmentKnowledgeBase treatmentKnowledgeBase;

    @Value("${krishimitra.storage.disease-images-dir:./uploads/disease-images}")
    private String uploadDir;

    private static final String MODEL_VERSION = "v1.0.0";

    @Transactional
    public DiseaseDetectionResponse analyzeImage(MultipartFile image, UUID farmId, String cropName) {
        if (image.isEmpty()) {
            throw new BadRequestException("Uploaded image cannot be empty");
        }

        try {
            // 1. Create upload directory if it does not exist
            Path uploadPath = Paths.get(uploadDir);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            // 2. Save file to disk with unique UUID name
            String fileExtension = getFileExtension(image.getOriginalFilename());
            String fileName = UUID.randomUUID().toString() + fileExtension;
            Path filePath = uploadPath.resolve(fileName);
            Files.write(filePath, image.getBytes());
            log.info("Saved disease detection image to {}", filePath);

            // 3. Perform ML inference
            byte[] imageBytes = image.getBytes();
            DiseaseOnnxModelService.PredictionResult prediction = onnxModelService.predict(imageBytes);

            // 4. Retrieve treatment and recommendations
            TreatmentKnowledgeBase.TreatmentInfo treatmentInfo = treatmentKnowledgeBase.getTreatment(prediction.diseaseClass());

            // 5. Save report to DB
            DiseaseReportEntity report = DiseaseReportEntity.builder()
                    .farmId(farmId)
                    .cropName(cropName)
                    .imageUrl("/api/v1/disease/images/" + fileName)
                    .detectedDisease(treatmentInfo.diseaseName())
                    .confidenceScore((double) prediction.confidence())
                    .severity(treatmentInfo.severity())
                    .recommendedAction(treatmentInfo.organicTreatment() + " | " + treatmentInfo.chemicalTreatment())
                    .recommendedActionHi(treatmentInfo.organicTreatment() + " (हिंदी में) | " + treatmentInfo.chemicalTreatment() + " (हिंदी में)")
                    .modelVersion(MODEL_VERSION)
                    .isConfirmed(false)
                    .build();

            DiseaseReportEntity savedReport = diseaseReportRepository.save(report);

            // 6. Return Response DTO
            return mapToResponse(savedReport);
        } catch (IOException e) {
            log.error("Failed to save/process disease image", e);
            throw new RuntimeException("Failed to analyze image due to storage or processing error", e);
        }
    }

    @Transactional(readOnly = true)
    public List<DiseaseDetectionResponse> getReports(UUID farmId) {
        return diseaseReportRepository.findByFarmIdOrderByCreatedAtDesc(farmId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    private String getFileExtension(String fileName) {
        if (fileName == null || !fileName.contains(".")) {
            return ".jpg";
        }
        return fileName.substring(fileName.lastIndexOf("."));
    }

    private DiseaseDetectionResponse mapToResponse(DiseaseReportEntity entity) {
        return DiseaseDetectionResponse.builder()
                .id(entity.getId())
                .farmId(entity.getFarmId())
                .cropName(entity.getCropName())
                .imageUrl(entity.getImageUrl())
                .detectedDisease(entity.getDetectedDisease())
                .confidenceScore(entity.getConfidenceScore())
                .severity(entity.getSeverity() != null ? entity.getSeverity().name() : "MODERATE")
                .recommendedAction(entity.getRecommendedAction())
                .recommendedActionHi(entity.getRecommendedActionHi())
                .createdAt(entity.getCreatedAt())
                .build();
    }
}
