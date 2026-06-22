package com.krishimitra.schemerecommendation.internal;

import com.krishimitra.shared.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.*;

import java.time.LocalDate;

/**
 * Represents a government agricultural scheme.
 * Eligibility criteria stored as JSON text for flexible rule matching.
 */
@Entity
@Table(name = "government_schemes")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GovernmentSchemeEntity extends BaseEntity {

    @Column(name = "scheme_name", nullable = false)
    private String schemeName;

    @Column(name = "scheme_name_hi")
    private String schemeNameHi;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "description_hi", columnDefinition = "TEXT")
    private String descriptionHi;

    @Column(name = "scheme_type", nullable = false, length = 50)
    private String schemeType;

    @Column(name = "eligibility_criteria", columnDefinition = "TEXT")
    private String eligibilityCriteria;

    @Column(name = "benefits", columnDefinition = "TEXT")
    private String benefits;

    @Column(name = "application_url")
    private String applicationUrl;

    @Column(name = "valid_from")
    private LocalDate validFrom;

    @Column(name = "valid_until")
    private LocalDate validUntil;

    @Column(name = "is_active", nullable = false)
    private boolean isActive;
}
