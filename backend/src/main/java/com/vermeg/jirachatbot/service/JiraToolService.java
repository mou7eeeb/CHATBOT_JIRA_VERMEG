package com.vermeg.jirachatbot.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vermeg.jirachatbot.entity.JiraConnection;
import com.vermeg.jirachatbot.model.JiraTicket;
import com.vermeg.jirachatbot.repository.JiraConnectionRepository;
import com.vermeg.jirachatbot.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
@SuppressWarnings({"unchecked", "rawtypes"})
public class JiraToolService {

    private final JiraConnectionRepository jiraConnectionRepository;
    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    private JiraConnection getActiveJiraConnection() {
        log.debug("=== Retrieving Active Jira Connection ===");
        
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal == null) {
            log.error("Authentication principal is null");
            throw new RuntimeException("User not authenticated. Please log in to access Jira features.");
        }
        
        UserPrincipal userPrincipal;
        try {
            userPrincipal = (UserPrincipal) principal;
        } catch (ClassCastException e) {
            log.error("Principal is not UserPrincipal, it's: {}", principal.getClass().getName());
            throw new RuntimeException("User not authenticated properly. Please log in again.");
        }
        
        Long userId = userPrincipal.getId();
        log.debug("Current User ID: {}", userId);

        // Try to get default connection first
        return jiraConnectionRepository.findByUserIdAndIsDefaultTrue(userId)
                .filter(JiraConnection::getIsActive)
                .map(conn -> {
                    log.debug("Found default Jira connection: {}", conn.getConnectionName());
                    log.debug("Jira Base URL: {}", conn.getJiraBaseUrl());
                    log.debug("Jira Email: {}", conn.getJiraEmail());
                    return conn;
                })
                .orElseGet(() -> {
                    // If no default, get the first active connection
                    List<JiraConnection> activeConnections = jiraConnectionRepository.findByUserIdAndIsActiveTrue(userId);
                    if (activeConnections.isEmpty()) {
                        log.warn("No active Jira connection found for user: {}", userId);
                        throw new RuntimeException("No active Jira connection found. Please connect a Jira account from the Jira Connections page.");
                    }
                    JiraConnection conn = activeConnections.get(0);
                    log.debug("Found first active Jira connection: {}", conn.getConnectionName());
                    log.debug("Jira Base URL: {}", conn.getJiraBaseUrl());
                    log.debug("Jira Email: {}", conn.getJiraEmail());
                    return conn;
                });
    }

    private HttpHeaders createAuthHeaders(JiraConnection connection) {
        log.debug("=== Creating Authorization Headers ===");
        HttpHeaders headers = new HttpHeaders();
        headers.set("Content-Type", "application/json");

        String auth = connection.getJiraEmail() + ":" + connection.getJiraApiToken();
        String encodedAuth = Base64.getEncoder().encodeToString(auth.getBytes(StandardCharsets.UTF_8));
        headers.set("Authorization", "Basic " + encodedAuth);

        log.debug("Authorization header created (length: {})", encodedAuth.length());
        return headers;
    }

    /**
     * Validates the Jira connection by calling /rest/api/3/myself
     * @throws RuntimeException with specific error message if validation fails
     */
    public void validateConnection() {
        log.info("=== Validating Jira Connection ===");
        long startTime = System.currentTimeMillis();

        try {
            JiraConnection connection = getActiveJiraConnection();
            String url = connection.getJiraBaseUrl() + "/rest/api/3/myself";

            log.debug("Validation URL: {}", url);
            log.debug("HTTP Method: GET");

            HttpHeaders headers = createAuthHeaders(connection);
            HttpEntity<Void> entity = new HttpEntity<>(headers);

            log.debug("Sending validation request to Jira...");
            ResponseEntity<Map> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    entity,
                    Map.class
            );

            long executionTime = System.currentTimeMillis() - startTime;
            log.info("Jira connection validation successful");
            log.info("HTTP Status: {}", response.getStatusCode());
            log.info("Execution Time: {}ms", executionTime);

            if (response.getBody() != null) {
                log.debug("Response Body Keys: {}", response.getBody().keySet());
            }

        } catch (HttpClientErrorException e) {
            long executionTime = System.currentTimeMillis() - startTime;
            log.error("Jira validation failed - Client Error");
            log.error("HTTP Status: {}", e.getStatusCode());
            log.error("Response Body: {}", e.getResponseBodyAsString());
            log.error("Execution Time: {}ms", executionTime);

            String errorMessage = parseJiraError(e.getResponseBodyAsString(), e.getStatusCode());
            throw new RuntimeException(errorMessage);

        } catch (HttpServerErrorException e) {
            long executionTime = System.currentTimeMillis() - startTime;
            log.error("Jira validation failed - Server Error");
            log.error("HTTP Status: {}", e.getStatusCode());
            log.error("Response Body: {}", e.getResponseBodyAsString());
            log.error("Execution Time: {}ms", executionTime);

            String errorMessage = parseJiraError(e.getResponseBodyAsString(), e.getStatusCode());
            throw new RuntimeException(errorMessage);

        } catch (ResourceAccessException e) {
            long executionTime = System.currentTimeMillis() - startTime;
            log.error("Jira validation failed - Network Error");
            log.error("Error: {}", e.getMessage());
            log.error("Execution Time: {}ms", executionTime);

            throw new RuntimeException("Jira server unavailable. Please check your network connection and Jira URL.");

        } catch (Exception e) {
            long executionTime = System.currentTimeMillis() - startTime;
            log.error("Jira validation failed - Unexpected Error");
            log.error("Error: {}", e.getMessage(), e);
            log.error("Execution Time: {}ms", executionTime);

            throw new RuntimeException("Jira validation failed: " + e.getMessage());
        }
    }

    private String parseJiraError(String responseBody, HttpStatusCode status) {
        log.debug("Parsing Jira error response");
        log.debug("Status: {}", status);
        log.debug("Response Body: {}", responseBody);

        if (responseBody == null || responseBody.isEmpty()) {
            return switch (status.value()) {
                case 401 -> "Invalid Jira API Token. Please check your Jira API token in the Jira Connections page.";
                case 403 -> "Jira authentication failed. Please check your email and API token.";
                case 404 -> "Invalid Jira URL. Please check your Jira Base URL in the Jira Connections page.";
                default -> "Jira server returned error " + status + ". Please check your Jira connection settings.";
            };
        }

        // Try to extract error message from Jira response
        if (responseBody.contains("errorMessages")) {
            try {
                Map<String, Object> errorMap = objectMapper.readValue(responseBody, Map.class);
                if (errorMap.containsKey("errorMessages")) {
                    @SuppressWarnings("unchecked")
                    List<String> errorMessages = (List<String>) errorMap.get("errorMessages");
                    if (!errorMessages.isEmpty()) {
                        return "Jira Error: " + errorMessages.get(0);
                    }
                }
            } catch (Exception e) {
                log.debug("Failed to parse Jira error response", e);
            }
        }

        return switch (status.value()) {
            case 401 -> "Invalid Jira API Token. Please check your Jira API token.";
            case 403 -> "Jira authentication failed. Please check your credentials.";
            case 404 -> "Invalid Jira URL or endpoint not found.";
            default -> "Jira server error: " + responseBody;
        };
    }

    public Map<String, Object> getCurrentUser() {
        log.info("=== Getting Current Jira User ===");
        long startTime = System.currentTimeMillis();

        try {
            validateConnection();
            JiraConnection connection = getActiveJiraConnection();
            String url = connection.getJiraBaseUrl() + "/rest/api/3/myself";

            log.debug("Request URL: {}", url);
            HttpHeaders headers = createAuthHeaders(connection);
            HttpEntity<Void> entity = new HttpEntity<>(headers);

            log.debug("Sending GET request to Jira...");
            ResponseEntity<Map> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    entity,
                    Map.class
            );

            long executionTime = System.currentTimeMillis() - startTime;
            log.info("Successfully retrieved current user");
            log.info("HTTP Status: {}", response.getStatusCode());
            log.info("Execution Time: {}ms", executionTime);

            return response.getBody();

        } catch (RuntimeException e) {
            long executionTime = System.currentTimeMillis() - startTime;
            log.error("Failed to retrieve current user from Jira");
            log.error("Execution Time: {}ms", executionTime);
            throw e;
        }
    }

    public List<Map<String, Object>> getProjects() {
        log.info("=== Getting Jira Projects ===");
        long startTime = System.currentTimeMillis();

        try {
            validateConnection();
            JiraConnection connection = getActiveJiraConnection();
            String url = connection.getJiraBaseUrl() + "/rest/api/3/project/search";

            log.debug("Request URL: {}", url);
            HttpHeaders headers = createAuthHeaders(connection);
            HttpEntity<Void> entity = new HttpEntity<>(headers);

            log.debug("Sending GET request to Jira...");
            ResponseEntity<Map> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    entity,
                    Map.class
            );

            long executionTime = System.currentTimeMillis() - startTime;
            log.info("HTTP Status: {}", response.getStatusCode());
            log.info("Execution Time: {}ms", executionTime);

            if (response.getBody() != null && response.getBody().containsKey("values")) {
                @SuppressWarnings("unchecked")
                List<Map<String, Object>> projects = (List<Map<String, Object>>) response.getBody().get("values");
                log.info("Found {} projects", projects.size());
                return projects;
            }

            log.warn("No projects found in Jira response");
            return List.of();

        } catch (RuntimeException e) {
            long executionTime = System.currentTimeMillis() - startTime;
            log.error("Failed to retrieve projects from Jira");
            log.error("Execution Time: {}ms", executionTime);
            throw e;
        }
    }

    public List<JiraTicket> executeJQL(String jqlQuery) {
        log.info("=== Executing JQL Query ===");
        log.info("JQL: {}", jqlQuery);
        long startTime = System.currentTimeMillis();

        try {
            validateConnection();
            JiraConnection connection = getActiveJiraConnection();
            String url = connection.getJiraBaseUrl() + "/rest/api/3/search/jql";

            log.debug("Request URL: {}", url);
            HttpHeaders headers = createAuthHeaders(connection);

            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("jql", jqlQuery);
            requestBody.put("maxResults", 50);
            requestBody.put("fields", List.of("summary", "status", "issuetype", "priority", "assignee", "reporter", "created", "updated", "description"));

            log.debug("Request Body: {}", requestBody);
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

            log.debug("Sending POST request to Jira...");
            ResponseEntity<Map> response = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    entity,
                    Map.class
            );

            long executionTime = System.currentTimeMillis() - startTime;
            log.info("HTTP Status: {}", response.getStatusCode());
            log.info("Execution Time: {}ms", executionTime);

            if (response.getBody() != null && response.getBody().containsKey("issues")) {
                @SuppressWarnings("unchecked")
                List<Map<String, Object>> issues = (List<Map<String, Object>>) response.getBody().get("issues");
                List<JiraTicket> tickets = issues.stream()
                        .map(issue -> objectMapper.convertValue(issue, JiraTicket.class))
                        .toList();

                log.info("Found {} tickets", tickets.size());
                return tickets;
            }

            log.warn("No issues found in Jira response");
            return List.of();

        } catch (RuntimeException e) {
            long executionTime = System.currentTimeMillis() - startTime;
            log.error("Failed to execute JQL query");
            log.error("Execution Time: {}ms", executionTime);
            throw e;
        }
    }

    public Map<String, Object> getIssue(String issueKey) {
        log.info("=== Getting Jira Issue ===");
        log.info("Issue Key: {}", issueKey);
        long startTime = System.currentTimeMillis();

        try {
            validateConnection();
            JiraConnection connection = getActiveJiraConnection();
            String url = connection.getJiraBaseUrl() + "/rest/api/3/issue/" + issueKey;

            log.debug("Request URL: {}", url);
            HttpHeaders headers = createAuthHeaders(connection);
            HttpEntity<Void> entity = new HttpEntity<>(headers);

            log.debug("Sending GET request to Jira...");
            ResponseEntity<Map> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    entity,
                    Map.class
            );

            long executionTime = System.currentTimeMillis() - startTime;
            log.info("HTTP Status: {}", response.getStatusCode());
            log.info("Execution Time: {}ms", executionTime);
            log.info("Successfully retrieved issue: {}", issueKey);

            return response.getBody();

        } catch (RuntimeException e) {
            long executionTime = System.currentTimeMillis() - startTime;
            log.error("Failed to retrieve issue from Jira");
            log.error("Execution Time: {}ms", executionTime);
            throw e;
        }
    }

    public List<JiraTicket> searchIssues(String jql) {
        return executeJQL(jql);
    }

    public List<Map<String, Object>> getBoards() {
        log.info("=== Getting Jira Boards ===");
        long startTime = System.currentTimeMillis();

        try {
            validateConnection();
            JiraConnection connection = getActiveJiraConnection();
            String url = connection.getJiraBaseUrl() + "/rest/agile/1.0/board";

            log.debug("Request URL: {}", url);
            HttpHeaders headers = createAuthHeaders(connection);
            HttpEntity<Void> entity = new HttpEntity<>(headers);

            log.debug("Sending GET request to Jira...");
            ResponseEntity<Map> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    entity,
                    Map.class
            );

            long executionTime = System.currentTimeMillis() - startTime;
            log.info("HTTP Status: {}", response.getStatusCode());
            log.info("Execution Time: {}ms", executionTime);

            if (response.getBody() != null && response.getBody().containsKey("values")) {
                @SuppressWarnings("unchecked")
                List<Map<String, Object>> boards = (List<Map<String, Object>>) response.getBody().get("values");
                log.info("Found {} boards", boards.size());
                return boards;
            }

            log.warn("No boards found in Jira response");
            return List.of();

        } catch (RuntimeException e) {
            long executionTime = System.currentTimeMillis() - startTime;
            log.error("Failed to retrieve boards from Jira");
            log.error("Execution Time: {}ms", executionTime);
            throw e;
        }
    }

    public List<Map<String, Object>> getSprints(Long boardId) {
        log.info("=== Getting Jira Sprints ===");
        log.info("Board ID: {}", boardId);
        long startTime = System.currentTimeMillis();

        try {
            validateConnection();
            JiraConnection connection = getActiveJiraConnection();
            String url = connection.getJiraBaseUrl() + "/rest/agile/1.0/board/" + boardId + "/sprint";

            log.debug("Request URL: {}", url);
            HttpHeaders headers = createAuthHeaders(connection);
            HttpEntity<Void> entity = new HttpEntity<>(headers);

            log.debug("Sending GET request to Jira...");
            ResponseEntity<Map> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    entity,
                    Map.class
            );

            long executionTime = System.currentTimeMillis() - startTime;
            log.info("HTTP Status: {}", response.getStatusCode());
            log.info("Execution Time: {}ms", executionTime);

            if (response.getBody() != null && response.getBody().containsKey("values")) {
                @SuppressWarnings("unchecked")
                List<Map<String, Object>> sprints = (List<Map<String, Object>>) response.getBody().get("values");
                log.info("Found {} sprints", sprints.size());
                return sprints;
            }

            log.warn("No sprints found in Jira response");
            return List.of();

        } catch (RuntimeException e) {
            long executionTime = System.currentTimeMillis() - startTime;
            log.error("Failed to retrieve sprints from Jira");
            log.error("Execution Time: {}ms", executionTime);
            throw e;
        }
    }
}
