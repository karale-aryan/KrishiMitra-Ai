package com.krishimitra.farm;

import com.krishimitra.farm.dto.FarmRequest;
import com.krishimitra.farm.dto.FarmResponse;
import com.krishimitra.shared.dto.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * REST controller for farm management.
 */
@RestController
@RequestMapping("/api/v1/farms")
@RequiredArgsConstructor
@Slf4j
public class FarmController {

    private final FarmService farmService;

    /**
     * Lists all farms belonging to a farmer.
     *
     * @param farmerId the farmer's ID (query parameter)
     * @return list of farm responses
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<FarmResponse>>> listFarms(@RequestParam UUID farmerId) {
        log.debug("GET /api/v1/farms - farmerId: {}", farmerId);

        List<FarmResponse> farms = farmService.listFarmsByFarmer(farmerId);
        return ResponseEntity.ok(ApiResponse.success("Farms retrieved successfully", farms));
    }

    /**
     * Retrieves a farm by its ID.
     *
     * @param id the farm ID
     * @return the farm response
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<FarmResponse>> getFarmById(@PathVariable UUID id) {
        log.debug("GET /api/v1/farms/{}", id);

        FarmResponse response = farmService.getFarmById(id);
        return ResponseEntity.ok(ApiResponse.success("Farm retrieved successfully", response));
    }

    /**
     * Creates a new farm.
     *
     * @param request the farm creation request (includes farmerId)
     * @return the created farm response
     */
    @PostMapping
    public ResponseEntity<ApiResponse<FarmResponse>> createFarm(@Valid @RequestBody FarmRequest request) {
        log.info("POST /api/v1/farms - farmName: {}, farmerId: {}", request.getFarmName(), request.getFarmerId());

        FarmResponse response = farmService.createFarm(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Farm created successfully", response));
    }

    /**
     * Updates an existing farm.
     *
     * @param id      the farm ID
     * @param request the update request
     * @return the updated farm response
     */
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<FarmResponse>> updateFarm(
            @PathVariable UUID id,
            @Valid @RequestBody FarmRequest request) {
        log.info("PUT /api/v1/farms/{}", id);

        FarmResponse response = farmService.updateFarm(id, request);
        return ResponseEntity.ok(ApiResponse.success("Farm updated successfully", response));
    }

    /**
     * Deletes a farm by its ID.
     *
     * @param id the farm ID
     * @return empty success response
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteFarm(@PathVariable UUID id) {
        log.info("DELETE /api/v1/farms/{}", id);

        farmService.deleteFarm(id);
        return ResponseEntity.ok(ApiResponse.success("Farm deleted successfully", null));
    }
}
