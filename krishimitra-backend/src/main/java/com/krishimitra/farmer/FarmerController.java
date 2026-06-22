package com.krishimitra.farmer;

import com.krishimitra.farmer.dto.FarmerRequest;
import com.krishimitra.farmer.dto.FarmerResponse;
import com.krishimitra.shared.dto.ApiResponse;
import com.krishimitra.shared.dto.PagedResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * REST controller for farmer profile management.
 */
@RestController
@RequestMapping("/api/v1/farmers")
@RequiredArgsConstructor
@Slf4j
public class FarmerController {

    private final FarmerService farmerService;

    /**
     * Lists all farmers with pagination. Restricted to ADMIN and AGRONOMIST roles.
     *
     * @param pageable pagination parameters (page, size, sort)
     * @return paginated list of farmer responses
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('AGRONOMIST')")
    public ResponseEntity<ApiResponse<PagedResponse<FarmerResponse>>> listFarmers(Pageable pageable) {
        log.debug("GET /api/v1/farmers - page: {}, size: {}", pageable.getPageNumber(), pageable.getPageSize());

        Page<FarmerResponse> page = farmerService.listFarmers(pageable);

        PagedResponse<FarmerResponse> pagedResponse = PagedResponse.of(
                page.getContent(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.isLast()
        );

        return ResponseEntity.ok(ApiResponse.success("Farmers retrieved successfully", pagedResponse));
    }

    /**
     * Retrieves the current authenticated user's farmer profile.
     *
     * @param authentication the security context authentication
     * @return the farmer response
     */
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<FarmerResponse>> getMyProfile(Authentication authentication) {
        UUID userId = UUID.fromString(authentication.getName());
        log.debug("GET /api/v1/farmers/me - userId: {}", userId);

        FarmerResponse response = farmerService.getFarmerByUserId(userId);
        return ResponseEntity.ok(ApiResponse.success("Farmer profile retrieved successfully", response));
    }

    /**
     * Retrieves a farmer profile by its ID.
     *
     * @param id the farmer ID
     * @return the farmer response
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<FarmerResponse>> getFarmerById(@PathVariable UUID id) {
        log.debug("GET /api/v1/farmers/{}", id);

        FarmerResponse response = farmerService.getFarmerById(id);
        return ResponseEntity.ok(ApiResponse.success("Farmer retrieved successfully", response));
    }

    /**
     * Creates a new farmer profile linked to the current authenticated user.
     *
     * @param request        the farmer creation request
     * @param authentication the security context authentication
     * @return the created farmer response
     */
    @PostMapping
    public ResponseEntity<ApiResponse<FarmerResponse>> createFarmer(
            @Valid @RequestBody FarmerRequest request,
            Authentication authentication) {
        UUID userId = UUID.fromString(authentication.getName());
        log.info("POST /api/v1/farmers - userId: {}", userId);

        FarmerResponse response = farmerService.createFarmer(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Farmer profile created successfully", response));
    }

    /**
     * Updates an existing farmer profile.
     *
     * @param id      the farmer ID
     * @param request the update request
     * @return the updated farmer response
     */
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<FarmerResponse>> updateFarmer(
            @PathVariable UUID id,
            @Valid @RequestBody FarmerRequest request) {
        log.info("PUT /api/v1/farmers/{}", id);

        FarmerResponse response = farmerService.updateFarmer(id, request);
        return ResponseEntity.ok(ApiResponse.success("Farmer profile updated successfully", response));
    }
}
