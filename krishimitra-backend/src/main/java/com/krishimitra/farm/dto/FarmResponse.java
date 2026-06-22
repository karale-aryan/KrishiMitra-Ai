package com.krishimitra.farm.dto;

import com.krishimitra.farm.internal.FarmEntity;
import com.krishimitra.farm.internal.FarmEntity.IrrigationType;
import com.krishimitra.farm.internal.FarmEntity.SoilType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Response DTO representing a farm.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FarmResponse {

    private UUID id;
    private UUID farmerId;
    private String farmName;
    private Double areaHectares;
    private Double latitude;
    private Double longitude;
    private SoilType soilType;
    private IrrigationType irrigationType;
    private Double soilPh;
    private Double nitrogenKgHa;
    private Double phosphorusKgHa;
    private Double potassiumKgHa;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    /**
     * Maps a {@link FarmEntity} to a {@link FarmResponse}.
     *
     * @param entity the farm entity
     * @return the populated response DTO
     */
    public static FarmResponse from(FarmEntity entity) {
        return FarmResponse.builder()
                .id(entity.getId())
                .farmerId(entity.getFarmerId())
                .farmName(entity.getFarmName())
                .areaHectares(entity.getAreaHectares())
                .latitude(entity.getLatitude())
                .longitude(entity.getLongitude())
                .soilType(entity.getSoilType())
                .irrigationType(entity.getIrrigationType())
                .soilPh(entity.getSoilPh())
                .nitrogenKgHa(entity.getNitrogenKgHa())
                .phosphorusKgHa(entity.getPhosphorusKgHa())
                .potassiumKgHa(entity.getPotassiumKgHa())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}
