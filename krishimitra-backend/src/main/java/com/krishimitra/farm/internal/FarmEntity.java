package com.krishimitra.farm.internal;

import com.krishimitra.shared.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

/**
 * JPA entity representing a farm owned by a farmer.
 */
@Entity
@Table(name = "farms", indexes = {
        @Index(name = "idx_farms_farmer_id", columnList = "farmer_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FarmEntity extends BaseEntity {

    @Column(name = "farmer_id", nullable = false)
    private UUID farmerId;

    @Column(name = "farm_name", nullable = false)
    private String farmName;

    @Column(name = "area_hectares", nullable = false)
    private Double areaHectares;

    @Column(name = "latitude")
    private Double latitude;

    @Column(name = "longitude")
    private Double longitude;

    @Enumerated(EnumType.STRING)
    @Column(name = "soil_type", nullable = false)
    private SoilType soilType;

    @Enumerated(EnumType.STRING)
    @Column(name = "irrigation_type", nullable = false)
    private IrrigationType irrigationType;

    @Column(name = "soil_ph")
    private Double soilPh;

    @Column(name = "nitrogen_kg_ha")
    private Double nitrogenKgHa;

    @Column(name = "phosphorus_kg_ha")
    private Double phosphorusKgHa;

    @Column(name = "potassium_kg_ha")
    private Double potassiumKgHa;

    /**
     * Types of soil found across Indian agricultural regions.
     */
    public enum SoilType {
        ALLUVIAL,
        BLACK,
        RED,
        LATERITE,
        DESERT,
        MOUNTAIN
    }

    /**
     * Irrigation methods used by Indian farmers.
     */
    public enum IrrigationType {
        CANAL,
        WELL,
        TUBE_WELL,
        DRIP,
        SPRINKLER,
        RAIN_FED
    }
}
