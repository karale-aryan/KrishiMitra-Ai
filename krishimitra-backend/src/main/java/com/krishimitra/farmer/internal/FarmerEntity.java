package com.krishimitra.farmer.internal;

import com.krishimitra.shared.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

/**
 * JPA entity representing a farmer profile linked to an authenticated user.
 */
@Entity
@Table(name = "farmers", indexes = {
        @Index(name = "idx_farmers_user_id", columnList = "user_id", unique = true),
        @Index(name = "idx_farmers_state", columnList = "state"),
        @Index(name = "idx_farmers_district", columnList = "district")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FarmerEntity extends BaseEntity {

    @Column(name = "user_id", nullable = false, unique = true)
    private UUID userId;

    @Column(name = "full_name", nullable = false)
    private String fullName;

    @Column(name = "aadhar_number", length = 12)
    private String aadharNumber;

    @Column(name = "state", nullable = false)
    private String state;

    @Column(name = "district", nullable = false)
    private String district;

    @Column(name = "village")
    private String village;

    @Column(name = "pincode", nullable = false, length = 6)
    private String pincode;

    @Column(name = "land_holding_hectares", nullable = false)
    private Double landHoldingHectares;

    @Enumerated(EnumType.STRING)
    @Column(name = "income_category", nullable = false)
    private IncomeCategory incomeCategory;

    /**
     * Annual income category brackets for Indian farmers.
     */
    public enum IncomeCategory {
        BELOW_1_LAKH,
        ONE_TO_THREE_LAKH,
        THREE_TO_FIVE_LAKH,
        ABOVE_FIVE_LAKH
    }
}
