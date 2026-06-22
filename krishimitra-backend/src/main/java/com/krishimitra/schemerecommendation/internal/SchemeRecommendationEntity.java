package com.krishimitra.schemerecommendation.internal;

import com.krishimitra.shared.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

/**
 * Represents a personalized scheme recommendation for a farmer.
 * Links a farmer to a matching government scheme with a computed score.
 */
@Entity
@Table(name = "scheme_recommendations")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SchemeRecommendationEntity extends BaseEntity {

    @Column(name = "farmer_id", nullable = false)
    private UUID farmerId;

    @Column(name = "scheme_id", nullable = false, insertable = false, updatable = false)
    private UUID schemeId;

    @Column(name = "match_score")
    private Integer matchScore;

    @Column(name = "match_reasons", columnDefinition = "TEXT")
    private String matchReasons;

    @Column(name = "status", length = 20, nullable = false)
    @Builder.Default
    private String status = "PENDING";

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "scheme_id", nullable = false)
    private GovernmentSchemeEntity scheme;
}
