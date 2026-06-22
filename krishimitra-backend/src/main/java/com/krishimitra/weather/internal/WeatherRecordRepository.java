package com.krishimitra.weather.internal;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface WeatherRecordRepository extends JpaRepository<WeatherRecordEntity, UUID> {

    List<WeatherRecordEntity> findByLatitudeAndLongitudeAndRecordedAtAfterOrderByRecordedAtDesc(
            Double latitude, Double longitude, LocalDateTime after);
}
