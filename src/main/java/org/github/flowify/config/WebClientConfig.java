package org.github.flowify.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {

    @Value("${app.fastapi.base-url}")
    private String fastapiBaseUrl;

    @Value("${app.fastapi.internal-token}")
    private String internalToken;

    @Bean
    public WebClient fastapiWebClient() {
        return WebClient.builder()
                .baseUrl(fastapiBaseUrl)
                .defaultHeader("X-Internal-Token", internalToken)
                .build();
    }
}
