package com.vermeg.jirachatbot.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "groq.api")
public class GroqConfig {

    private String key;
    private String url;
    private String model;

    public boolean isConfigured() {
        return key != null && !key.isEmpty() && !key.equals("your-groq-api-key-here");
    }
}
