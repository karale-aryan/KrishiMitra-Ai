package com.krishimitra.diseasedetection.internal;

import com.krishimitra.shared.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "disease_reports")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DiseaseReportEntity extends BaseEntity {

    @Column(name = "farm_id", nullable = false)
    private UUID farmId;

    @Column(name = "crop_name")
    private String cropName;

    @Column(name = "image_url")
    private String imageUrl;

    @Column(name = "detected_disease")
    private String detectedDisease;

    @Column(name = "confidence_score")
    private Double confidenceScore;

    @Column(name = "model_version")
    private String modelVersion;

    @Enumerated(EnumType.STRING)
    @Column(name = "severity")
    private Severity severity;

    @Column(name = "recommended_action", columnDefinition = "TEXT")
    private String recommendedAction;

    @Column(name = "recommended_action_hi", columnDefinition = "TEXT")
    private String recommendedActionHi;

    @Builder.Default
    @Column(name = "is_confirmed", nullable = false)
    private boolean isConfirmed = false;

    public enum Severity {
        LOW,
        MODERATE,
        SEVERE,
        CRITICAL
    }
}
