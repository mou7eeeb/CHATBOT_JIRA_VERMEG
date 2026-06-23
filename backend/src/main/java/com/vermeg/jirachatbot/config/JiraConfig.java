package com.vermeg.jirachatbot.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "jira")
public class JiraConfig {
    
    private String domain;
    private String email;
    private String apiToken;
    
    public String getApiUrl() {
        return domain + "/rest/api/3/search";
    }
}
