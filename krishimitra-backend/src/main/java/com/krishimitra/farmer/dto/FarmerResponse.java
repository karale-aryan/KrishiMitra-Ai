package com.krishimitra.farmer.dto;

import com.krishimitra.farmer.internal.FarmerEntity;
import com.krishimitra.farmer.internal.FarmerEntity.IncomeCategory;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Response DTO representing a farmer profile.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FarmerResponse {

    private UUID id;
    private UUID userId;
    private String fullName;
    private String state;
    private String district;
    private String village;
    private String pincode;
    private Double landHoldingHectares;
    private IncomeCategory incomeCategory;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    /**
     * Maps a {@link FarmerEntity} to a {@link FarmerResponse}.
     *
     * @param entity the farmer entity
     * @return the populated response DTO
     */
    public static FarmerResponse from(FarmerEntity entity) {
        return FarmerResponse.builder()
                .id(entity.getId())
                .userId(entity.getUserId())
                .fullName(entity.getFullName())
                .state(entity.getState())
                .district(entity.getDistrict())
                .village(entity.getVillage())
                .pincode(entity.getPincode())
                .landHoldingHectares(entity.getLandHoldingHectares())
                .incomeCategory(entity.getIncomeCategory())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}
