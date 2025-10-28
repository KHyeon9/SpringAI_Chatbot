package com.education.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Deprecated
@Configuration
public class GeminiConfig {
    @Value("${google.api.key}")
    private String apiKey;

//    @Bean
//    public Client getClient() {
//        return Client.builder().apiKey(apiKey).build();
//    }
}
