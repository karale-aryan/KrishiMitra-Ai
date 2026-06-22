package com.krishimitra.voiceassistant.internal;

import com.krishimitra.shared.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.*;

import java.util.UUID;

/**
 * Persists every advisory interaction (voice or text) for audit trail and analytics.
 */
@Entity
@Table(name = "advisory_logs")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdvisoryLogEntity extends BaseEntity {

    @Column(name = "farmer_id")
    private UUID farmerId;

    @Column(name = "advisory_type", nullable = false)
    private String advisoryType;

    @Column(name = "query_text", columnDefinition = "TEXT")
    private String queryText;

    @Column(name = "response_text", columnDefinition = "TEXT")
    private String responseText;

    @Column(name = "query_language", length = 10)
    private String queryLanguage;

    @Column(name = "response_language", length = 10)
    private String responseLanguage;

    @Column(name = "input_mode", length = 10, nullable = false)
    private String inputMode;

    @Column(name = "session_id")
    private String sessionId;

    @Column(name = "response_time_ms")
    private Long responseTimeMs;
}
