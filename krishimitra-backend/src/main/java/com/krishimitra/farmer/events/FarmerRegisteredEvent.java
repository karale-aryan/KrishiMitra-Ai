package com.krishimitra.farmer.events;

import com.krishimitra.farmer.internal.FarmerEntity.IncomeCategory;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * Domain event published when a new farmer profile is registered.
 * Other modules can listen to this event via Spring's {@code @EventListener}.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FarmerRegisteredEvent {

    private UUID farmerId;
    private UUID userId;
    private String state;
    private String district;
    private Double landHoldingHectares;
    private IncomeCategory incomeCategory;
}
