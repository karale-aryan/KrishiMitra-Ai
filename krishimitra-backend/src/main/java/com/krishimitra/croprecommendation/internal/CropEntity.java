package com.krishimitra.croprecommendation.internal;

import com.krishimitra.shared.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "crops")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CropEntity extends BaseEntity {

    @Column(name = "crop_name", nullable = false, unique = true)
    private String cropName;

    @Column(name = "crop_name_hi")
    private String cropNameHi;

    @Column(name = "crop_name_mr")
    private String cropNameMr;

    @Column(name = "crop_name_te")
    private String cropNameTe;

    @Column(name = "crop_name_kn")
    private String cropNameKn;

    @Enumerated(EnumType.STRING)
    @Column(name = "crop_type")
    private CropType cropType;

    @Column(name = "ideal_temp_min")
    private Double idealTempMin;

    @Column(name = "ideal_temp_max")
    private Double idealTempMax;

    @Column(name = "ideal_humidity_min")
    private Double idealHumidityMin;

    @Column(name = "ideal_humidity_max")
    private Double idealHumidityMax;

    @Column(name = "ideal_ph_min")
    private Double idealPhMin;

    @Column(name = "ideal_ph_max")
    private Double idealPhMax;

    @Column(name = "ideal_rainfall_mm")
    private Double idealRainfallMm;

    @Column(name = "growing_season_days")
    private Integer growingSeasonDays;

    public enum CropType {
        KHARIF,
        RABI,
        ZAID
    }
}
