package com.krishimitra.shared.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

import java.time.Duration;

/**
 * WebClient beans for communicating with the Python AI sidecar and Open-Meteo weather API.
 */
@Configuration
public class WebClientConfig {

    @Value("${krishimitra.ai-sidecar.base-url}")
    private String aiSidecarBaseUrl;

    @Value("${krishimitra.ai-sidecar.connect-timeout-ms}")
    private int aiConnectTimeout;

    @Value("${krishimitra.ai-sidecar.read-timeout-ms}")
    private int aiReadTimeout;

    @Value("${krishimitra.weather.base-url}")
    private String weatherBaseUrl;

    @Value("${krishimitra.weather.connect-timeout-ms}")
    private int weatherConnectTimeout;

    @Value("${krishimitra.weather.read-timeout-ms}")
    private int weatherReadTimeout;

    /**
     * WebClient for Python AI sidecar (Whisper, IndicTrans2, Indic-TTS).
     * Longer read timeout (120s) to accommodate ML model inference.
     */
    @Bean(name = "aiSidecarClient")
    public WebClient aiSidecarClient(WebClient.Builder builder) {
        HttpClient httpClient = HttpClient.create()
                .responseTimeout(Duration.ofMillis(aiReadTimeout))
                .option(io.netty.channel.ChannelOption.CONNECT_TIMEOUT_MILLIS, aiConnectTimeout);

        return builder
                .baseUrl(aiSidecarBaseUrl)
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(50 * 1024 * 1024)) // 50MB for audio
                .build();
    }

    /**
     * WebClient for Open-Meteo weather API.
     * Shorter timeout since weather API is lightweight.
     */
    @Bean(name = "weatherClient")
    public WebClient weatherClient(WebClient.Builder builder) {
        HttpClient httpClient = HttpClient.create()
                .responseTimeout(Duration.ofMillis(weatherReadTimeout))
                .option(io.netty.channel.ChannelOption.CONNECT_TIMEOUT_MILLIS, weatherConnectTimeout);

        return builder
                .baseUrl(weatherBaseUrl)
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .build();
    }
}
