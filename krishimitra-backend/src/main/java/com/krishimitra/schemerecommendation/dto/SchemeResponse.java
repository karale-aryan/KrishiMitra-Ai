package com.krishimitra.schemerecommendation.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.UUID;

/**
 * Response DTO for a single government scheme.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SchemeResponse {

    private UUID id;
    private String schemeName;
    private String schemeNameHi;
    private String description;
    private String descriptionHi;
    private String schemeType;
    private String benefits;
    private String applicationUrl;
    private LocalDate validFrom;
    private LocalDate validUntil;
}
