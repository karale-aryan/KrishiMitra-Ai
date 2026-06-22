package com.krishimitra.weather.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WeatherResponse {

    private Double temperature;
    private Double humidity;
    private Double precipitation;
    private Double windSpeed;
    private String description;
    private LocalDateTime timestamp;
}
