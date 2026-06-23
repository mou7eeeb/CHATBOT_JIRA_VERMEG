package com.vermeg.jirachatbot.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class JiraConnectionDTO {
    private Long id;
    
    @NotBlank(message = "Connection name is required")
    private String connectionName;
    
    @NotBlank(message = "Jira base URL is required")
    @Pattern(regexp = "^https://.*\\.atlassian\\.net$", message = "Must be a valid Atlassian URL")
    private String jiraBaseUrl;
    
    @NotBlank(message = "Jira email is required")
    private String jiraEmail;
    
    @NotBlank(message = "Jira API token is required")
    private String jiraApiToken;
    
    private Boolean isDefault;
    private Boolean isActive;
    private LocalDateTime lastTestedAt;
    private Boolean lastTestSuccess;
    private String lastTestMessage;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
