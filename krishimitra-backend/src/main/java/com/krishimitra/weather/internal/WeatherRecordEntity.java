package com.krishimitra.weather.internal;

import com.krishimitra.shared.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "weather_records")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WeatherRecordEntity extends BaseEntity {

    @Column(name = "latitude", nullable = false)
    private Double latitude;

    @Column(name = "longitude", nullable = false)
    private Double longitude;

    @Column(name = "temperature")
    private Double temperature;

    @Column(name = "humidity")
    private Double humidity;

    @Column(name = "precipitation")
    private Double precipitation;

    @Column(name = "wind_speed")
    private Double windSpeed;

    @Column(name = "recorded_at", nullable = false)
    private LocalDateTime recordedAt;
}
