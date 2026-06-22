package com.krishimitra.farmer.dto;

import com.krishimitra.farmer.internal.FarmerEntity.IncomeCategory;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for creating or updating a farmer profile.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FarmerRequest {

    @NotBlank(message = "Full name is required")
    private String fullName;

    @Pattern(regexp = "^\\d{12}$", message = "Aadhar number must be exactly 12 digits")
    private String aadharNumber;

    @NotBlank(message = "State is required")
    private String state;

    @NotBlank(message = "District is required")
    private String district;

    private String village;

    @NotBlank(message = "Pincode is required")
    @Pattern(regexp = "^\\d{6}$", message = "Pincode must be exactly 6 digits")
    private String pincode;

    @NotNull(message = "Land holding in hectares is required")
    @Positive(message = "Land holding must be a positive number")
    private Double landHoldingHectares;

    @NotNull(message = "Income category is required")
    private IncomeCategory incomeCategory;
}
