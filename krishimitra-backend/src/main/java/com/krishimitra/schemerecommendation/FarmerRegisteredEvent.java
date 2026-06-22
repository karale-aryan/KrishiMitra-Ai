package com.krishimitra.schemerecommendation;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * Event published when a new farmer registers.
 * Used to trigger automatic scheme recommendation generation.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FarmerRegisteredEvent {

    private UUID farmerId;
    private String state;
    private String district;
    private Double landHolding;
    private String incomeCategory;
}
