package com.krishimitra.farm;

import com.krishimitra.farm.dto.FarmRequest;
import com.krishimitra.farm.dto.FarmResponse;
import com.krishimitra.farm.internal.FarmEntity;
import com.krishimitra.farm.internal.FarmRepository;
import com.krishimitra.farmer.internal.FarmerRepository;
import com.krishimitra.shared.exception.BadRequestException;
import com.krishimitra.shared.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Service layer for farm management.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class FarmService {

    private final FarmRepository farmRepository;
    private final FarmerRepository farmerRepository;

    /**
     * Lists all farms belonging to a specific farmer.
     *
     * @param farmerId the farmer's ID
     * @return list of farm responses
     */
    @Transactional(readOnly = true)
    public List<FarmResponse> listFarmsByFarmer(UUID farmerId) {
        log.debug("Listing farms for farmerId: {}", farmerId);
        return farmRepository.findByFarmerId(farmerId)
                .stream()
                .map(FarmResponse::from)
                .collect(Collectors.toList());
    }

    /**
     * Retrieves a farm by its ID.
     *
     * @param id the farm ID
     * @return the farm response
     * @throws ResourceNotFoundException if no farm is found with the given ID
     */
    @Transactional(readOnly = true)
    public FarmResponse getFarmById(UUID id) {
        log.debug("Fetching farm by id: {}", id);
        FarmEntity entity = farmRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Farm", "id", id));
        return FarmResponse.from(entity);
    }

    /**
     * Creates a new farm. Validates that the referenced farmer exists.
     *
     * @param request the farm creation request
     * @return the created farm response
     * @throws BadRequestException if the referenced farmer does not exist
     */
    @Transactional
    public FarmResponse createFarm(FarmRequest request) {
        log.info("Creating farm '{}' for farmerId: {}", request.getFarmName(), request.getFarmerId());

        if (!farmerRepository.existsById(request.getFarmerId())) {
            throw new BadRequestException("Farmer not found with id: " + request.getFarmerId());
        }

        FarmEntity entity = FarmEntity.builder()
                .farmerId(request.getFarmerId())
                .farmName(request.getFarmName())
                .areaHectares(request.getAreaHectares())
                .latitude(request.getLatitude())
                .longitude(request.getLongitude())
                .soilType(request.getSoilType())
                .irrigationType(request.getIrrigationType())
                .soilPh(request.getSoilPh())
                .nitrogenKgHa(request.getNitrogenKgHa())
                .phosphorusKgHa(request.getPhosphorusKgHa())
                .potassiumKgHa(request.getPotassiumKgHa())
                .build();

        FarmEntity saved = farmRepository.save(entity);
        log.info("Created farm with id: {} for farmerId: {}", saved.getId(), saved.getFarmerId());

        return FarmResponse.from(saved);
    }

    /**
     * Updates an existing farm.
     *
     * @param id      the farm ID
     * @param request the update request
     * @return the updated farm response
     * @throws ResourceNotFoundException if no farm is found with the given ID
     */
    @Transactional
    public FarmResponse updateFarm(UUID id, FarmRequest request) {
        log.info("Updating farm with id: {}", id);

        FarmEntity entity = farmRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Farm", "id", id));

        entity.setFarmerId(request.getFarmerId());
        entity.setFarmName(request.getFarmName());
        entity.setAreaHectares(request.getAreaHectares());
        entity.setLatitude(request.getLatitude());
        entity.setLongitude(request.getLongitude());
        entity.setSoilType(request.getSoilType());
        entity.setIrrigationType(request.getIrrigationType());
        entity.setSoilPh(request.getSoilPh());
        entity.setNitrogenKgHa(request.getNitrogenKgHa());
        entity.setPhosphorusKgHa(request.getPhosphorusKgHa());
        entity.setPotassiumKgHa(request.getPotassiumKgHa());

        FarmEntity updated = farmRepository.save(entity);
        log.info("Updated farm with id: {}", updated.getId());

        return FarmResponse.from(updated);
    }

    /**
     * Deletes a farm by its ID.
     *
     * @param id the farm ID
     * @throws ResourceNotFoundException if no farm is found with the given ID
     */
    @Transactional
    public void deleteFarm(UUID id) {
        log.info("Deleting farm with id: {}", id);

        if (!farmRepository.existsById(id)) {
            throw new ResourceNotFoundException("Farm", "id", id);
        }

        farmRepository.deleteById(id);
        log.info("Deleted farm with id: {}", id);
    }
}
