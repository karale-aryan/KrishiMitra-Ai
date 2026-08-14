package com.krishimitra.health;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

/**
 * Scheduled task that pings the health endpoint every 5 minutes
 * to prevent Render from putting the service to sleep.
 */
@Slf4j
@Component
public class SelfPingScheduler {

    private final RestTemplate restTemplate;
    private final String baseUrl;

    public SelfPingScheduler(
            @Value("${app.self-ping.base-url:}") String baseUrl) {
        this.restTemplate = new RestTemplate();
        this.baseUrl = baseUrl;
    }

    /**
     * Pings the health endpoint every 5 minutes (300,000 ms).
     * Only runs if `app.self-ping.base-url` is configured.
     */
    @Scheduled(fixedRate = 300_000, initialDelay = 60_000)
    public void keepAlive() {
        if (baseUrl == null || baseUrl.isBlank()) {
            log.debug("Self-ping disabled: app.self-ping.base-url is not configured");
            return;
        }

        String healthUrl = baseUrl + "/api/health";
        try {
            restTemplate.getForObject(healthUrl, String.class);
            log.info("Self-ping successful: {}", healthUrl);
        } catch (Exception e) {
            log.warn("Self-ping failed for {}: {}", healthUrl, e.getMessage());
        }
    }
}
