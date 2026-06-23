package com.vermeg.jirachatbot.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vermeg.jirachatbot.config.JiraConfig;
import com.vermeg.jirachatbot.model.JiraTicket;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class JiraService {
    
    private final JiraConfig jiraConfig;
    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    public List<JiraTicket> searchTickets(String jqlQuery) {
        log.info("Searching Jira tickets with JQL: {}", jqlQuery);
        
        try {
            HttpHeaders headers = createAuthHeaders();
            
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("jql", jqlQuery);
            requestBody.put("maxResults", 50);
            requestBody.put("fields", List.of("summary", "status", "issuetype", "priority", "assignee", "reporter", "created", "updated", "description"));
            
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);
            
            ResponseEntity<Map> response = restTemplate.exchange(
                jiraConfig.getApiUrl(),
                HttpMethod.POST,
                entity,
                Map.class
            );
            
            if (response.getBody() != null && response.getBody().containsKey("issues")) {
                List<Map<String, Object>> issues = (List<Map<String, Object>>) response.getBody().get("issues");
                List<JiraTicket> tickets = issues.stream()
                    .map(issue -> objectMapper.convertValue(issue, JiraTicket.class))
                    .toList();
                
                log.info("Found {} tickets", tickets.size());
                return tickets;
            }
            
            log.warn("No issues found in Jira response");
            return List.of();
            
        } catch (Exception e) {
            log.error("Error searching Jira tickets: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to search Jira tickets: " + e.getMessage());
        }
    }
    
    private HttpHeaders createAuthHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Content-Type", "application/json");
        
        String auth = jiraConfig.getEmail() + ":" + jiraConfig.getApiToken();
        String encodedAuth = Base64.getEncoder().encodeToString(auth.getBytes(StandardCharsets.UTF_8));
        headers.set("Authorization", "Basic " + encodedAuth);
        
        return headers;
    }
}
