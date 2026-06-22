package com.krishimitra.farmer;

import com.krishimitra.farmer.dto.FarmerRequest;
import com.krishimitra.farmer.dto.FarmerResponse;
import com.krishimitra.farmer.events.FarmerRegisteredEvent;
import com.krishimitra.farmer.internal.FarmerEntity;
import com.krishimitra.farmer.internal.FarmerRepository;
import com.krishimitra.shared.exception.BadRequestException;
import com.krishimitra.shared.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Service layer for farmer profile management.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class FarmerService {

    private final FarmerRepository farmerRepository;
    private final ApplicationEventPublisher eventPublisher;

    /**
     * Lists all farmers with pagination.
     *
     * @param pageable pagination parameters
     * @return a page of farmer responses
     */
    @Transactional(readOnly = true)
    public Page<FarmerResponse> listFarmers(Pageable pageable) {
        log.debug("Listing farmers with pageable: {}", pageable);
        return farmerRepository.findAll(pageable)
                .map(FarmerResponse::from);
    }

    /**
     * Retrieves a farmer profile by the linked user ID.
     *
     * @param userId the authenticated user's ID
     * @return the farmer response
     * @throws ResourceNotFoundException if no farmer profile exists for the user
     */
    @Transactional(readOnly = true)
    public FarmerResponse getFarmerByUserId(UUID userId) {
        log.debug("Fetching farmer profile for userId: {}", userId);
        FarmerEntity entity = farmerRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Farmer", "userId", userId));
        return FarmerResponse.from(entity);
    }

    /**
     * Retrieves a farmer profile by its ID.
     *
     * @param id the farmer ID
     * @return the farmer response
     * @throws ResourceNotFoundException if no farmer is found with the given ID
     */
    @Transactional(readOnly = true)
    public FarmerResponse getFarmerById(UUID id) {
        log.debug("Fetching farmer by id: {}", id);
        FarmerEntity entity = farmerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Farmer", "id", id));
        return FarmerResponse.from(entity);
    }

    /**
     * Creates a new farmer profile linked to the authenticated user.
     * Validates that no duplicate profile exists for the user and publishes
     * a {@link FarmerRegisteredEvent} upon successful creation.
     *
     * @param userId  the authenticated user's ID
     * @param request the farmer creation request
     * @return the created farmer response
     * @throws BadRequestException if a farmer profile already exists for the user
     */
    @Transactional
    public FarmerResponse createFarmer(UUID userId, FarmerRequest request) {
        log.info("Creating farmer profile for userId: {}", userId);

        if (farmerRepository.existsByUserId(userId)) {
            throw new BadRequestException("Farmer profile already exists for user: " + userId);
        }

        FarmerEntity entity = FarmerEntity.builder()
                .userId(userId)
                .fullName(request.getFullName())
                .aadharNumber(request.getAadharNumber())
                .state(request.getState())
                .district(request.getDistrict())
                .village(request.getVillage())
                .pincode(request.getPincode())
                .landHoldingHectares(request.getLandHoldingHectares())
                .incomeCategory(request.getIncomeCategory())
                .build();

        FarmerEntity saved = farmerRepository.save(entity);
        log.info("Created farmer profile with id: {} for userId: {}", saved.getId(), userId);

        FarmerRegisteredEvent event = FarmerRegisteredEvent.builder()
                .farmerId(saved.getId())
                .userId(saved.getUserId())
                .state(saved.getState())
                .district(saved.getDistrict())
                .landHoldingHectares(saved.getLandHoldingHectares())
                .incomeCategory(saved.getIncomeCategory())
                .build();
        eventPublisher.publishEvent(event);
        log.debug("Published FarmerRegisteredEvent for farmerId: {}", saved.getId());

        return FarmerResponse.from(saved);
    }

    /**
     * Updates an existing farmer profile.
     *
     * @param id      the farmer ID
     * @param request the update request
     * @return the updated farmer response
     * @throws ResourceNotFoundException if no farmer is found with the given ID
     */
    @Transactional
    public FarmerResponse updateFarmer(UUID id, FarmerRequest request) {
        log.info("Updating farmer profile with id: {}", id);

        FarmerEntity entity = farmerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Farmer", "id", id));

        entity.setFullName(request.getFullName());
        entity.setAadharNumber(request.getAadharNumber());
        entity.setState(request.getState());
        entity.setDistrict(request.getDistrict());
        entity.setVillage(request.getVillage());
        entity.setPincode(request.getPincode());
        entity.setLandHoldingHectares(request.getLandHoldingHectares());
        entity.setIncomeCategory(request.getIncomeCategory());

        FarmerEntity updated = farmerRepository.save(entity);
        log.info("Updated farmer profile with id: {}", updated.getId());

        return FarmerResponse.from(updated);
    }
}
