package com.krishimitra.farm.dto;

import com.krishimitra.farm.internal.FarmEntity.IrrigationType;
import com.krishimitra.farm.internal.FarmEntity.SoilType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * Request DTO for creating or updating a farm.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FarmRequest {

    @NotNull(message = "Farmer ID is required")
    private UUID farmerId;

    @NotBlank(message = "Farm name is required")
    private String farmName;

    @NotNull(message = "Area in hectares is required")
    @Positive(message = "Area must be a positive number")
    private Double areaHectares;

    private Double latitude;

    private Double longitude;

    @NotNull(message = "Soil type is required")
    private SoilType soilType;

    @NotNull(message = "Irrigation type is required")
    private IrrigationType irrigationType;

    private Double soilPh;

    private Double nitrogenKgHa;

    private Double phosphorusKgHa;

    private Double potassiumKgHa;
}
