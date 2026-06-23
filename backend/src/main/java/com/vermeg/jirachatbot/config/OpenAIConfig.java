package com.vermeg.jirachatbot.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "openai.api")
public class OpenAIConfig {
    
    private String key;
    private String url;
    private String model;
    
    public boolean isConfigured() {
        return key != null && !key.isEmpty() && !key.equals("your-openai-api-key-here");
    }
}
