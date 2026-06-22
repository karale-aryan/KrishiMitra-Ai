package com.krishimitra.croprecommendation.internal;

import com.krishimitra.shared.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "crop_recommendations")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CropRecommendationEntity extends BaseEntity {

    @Column(name = "farm_id", nullable = false)
    private UUID farmId;

    @Column(name = "crop_id", nullable = false)
    private UUID cropId;

    @Column(name = "confidence_score", nullable = false)
    private Double confidenceScore;

    @Column(name = "model_version")
    private String modelVersion;

    @Column(name = "season")
    private String season;

    @Column(name = "input_features", columnDefinition = "TEXT")
    private String inputFeatures;

    @Builder.Default
    @Column(name = "is_accepted", nullable = false)
    private boolean isAccepted = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "crop_id", insertable = false, updatable = false)
    private CropEntity crop;
}
