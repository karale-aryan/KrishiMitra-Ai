package com.krishimitra.diseasedetection;

import com.krishimitra.diseasedetection.dto.DiseaseDetectionResponse;
import com.krishimitra.shared.dto.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.net.MalformedURLException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/v1/disease")
@RequiredArgsConstructor
public class DiseaseDetectionController {

    private final DiseaseDetectionService diseaseDetectionService;

    @Value("${krishimitra.storage.disease-images-dir:./uploads/disease-images}")
    private String uploadDir;

    @PostMapping(value = "/analyze", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<DiseaseDetectionResponse>> analyzeImage(
            @RequestParam("image") MultipartFile image,
            @RequestParam("farmId") UUID farmId,
            @RequestParam("cropName") String cropName) {

        log.info("Request received to analyze image for farm: {}, crop: {}", farmId, cropName);
        DiseaseDetectionResponse response = diseaseDetectionService.analyzeImage(image, farmId, cropName);
        return ResponseEntity.ok(ApiResponse.success("Image analyzed successfully", response));
    }

    @GetMapping("/reports/{farmId}")
    public ResponseEntity<ApiResponse<List<DiseaseDetectionResponse>>> getReports(@PathVariable UUID farmId) {
        log.info("Fetching disease reports for farm: {}", farmId);
        List<DiseaseDetectionResponse> reports = diseaseDetectionService.getReports(farmId);
        return ResponseEntity.ok(ApiResponse.success(reports));
    }

    @GetMapping("/images/{filename:.+}")
    public ResponseEntity<Resource> serveImage(@PathVariable String filename) {
        try {
            Path file = Paths.get(uploadDir).resolve(filename);
            Resource resource = new UrlResource(file.toUri());

            if (resource.exists() || resource.isReadable()) {
                return ResponseEntity.ok()
                        .contentType(MediaType.IMAGE_JPEG)
                        .body(resource);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (MalformedURLException e) {
            log.error("Error serving image file: {}", filename, e);
            return ResponseEntity.badRequest().build();
        }
    }
}
