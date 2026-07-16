package com.vermeg.jirachatbot.service;

import com.vermeg.jirachatbot.dto.JiraConnectionDTO;
import com.vermeg.jirachatbot.entity.JiraConnection;
import com.vermeg.jirachatbot.entity.User;
import com.vermeg.jirachatbot.repository.JiraConnectionRepository;
import com.vermeg.jirachatbot.repository.UserRepository;
import com.vermeg.jirachatbot.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class JiraConnectionService {
    
    private final JiraConnectionRepository jiraConnectionRepository;
    private final UserRepository userRepository;
    private final RestTemplate restTemplate = new RestTemplate();
    
    private Long getCurrentUserId() {
        UserPrincipal userPrincipal = (UserPrincipal) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return userPrincipal.getId();
    }
    
    @Transactional
    public JiraConnectionDTO createConnection(JiraConnectionDTO dto) {
        Long userId = getCurrentUserId();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        if (Boolean.TRUE.equals(dto.getIsDefault())) {
            jiraConnectionRepository.findByUserIdAndIsDefaultTrue(userId)
                    .ifPresent(conn -> {
                        conn.setIsDefault(false);
                        jiraConnectionRepository.save(conn);
                    });
        }
        
        JiraConnection connection = JiraConnection.builder()
                .user(user)
                .connectionName(dto.getConnectionName())
                .jiraBaseUrl(dto.getJiraBaseUrl())
                .jiraEmail(dto.getJiraEmail())
                .jiraApiToken(dto.getJiraApiToken())
                .isDefault(dto.getIsDefault() != null ? dto.getIsDefault() : false)
                .isActive(true)
                .build();
        
        connection = jiraConnectionRepository.save(connection);
        log.info("Created Jira connection: {} for user: {}", connection.getConnectionName(), userId);
        
        return mapToDTO(connection);
    }
    
    public List<JiraConnectionDTO> getAllConnections() {
        Long userId = getCurrentUserId();
        return jiraConnectionRepository.findByUserId(userId).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }
    
    public JiraConnectionDTO getConnection(Long id) {
        Long userId = getCurrentUserId();
        JiraConnection connection = jiraConnectionRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new RuntimeException("Connection not found"));
        return mapToDTO(connection);
    }
    
    @Transactional
    public JiraConnectionDTO updateConnection(Long id, JiraConnectionDTO dto) {
        Long userId = getCurrentUserId();
        JiraConnection connection = jiraConnectionRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new RuntimeException("Connection not found"));
        
        if (Boolean.TRUE.equals(dto.getIsDefault()) && !connection.getIsDefault()) {
            jiraConnectionRepository.findByUserIdAndIsDefaultTrue(userId)
                    .ifPresent(conn -> {
                        conn.setIsDefault(false);
                        jiraConnectionRepository.save(conn);
                    });
        }
        
        connection.setConnectionName(dto.getConnectionName());
        connection.setJiraBaseUrl(dto.getJiraBaseUrl());
        connection.setJiraEmail(dto.getJiraEmail());
        connection.setJiraApiToken(dto.getJiraApiToken());
        connection.setIsDefault(dto.getIsDefault() != null ? dto.getIsDefault() : connection.getIsDefault());
        connection.setIsActive(dto.getIsActive() != null ? dto.getIsActive() : connection.getIsActive());
        
        connection = jiraConnectionRepository.save(connection);
        log.info("Updated Jira connection: {}", connection.getId());
        
        return mapToDTO(connection);
    }
    
    @Transactional
    public void deleteConnection(Long id) {
        Long userId = getCurrentUserId();
        JiraConnection connection = jiraConnectionRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new RuntimeException("Connection not found"));
        
        jiraConnectionRepository.delete(connection);
        log.info("Deleted Jira connection: {}", id);
    }
    
    @Transactional
    public JiraConnectionDTO testConnection(Long id) {
        Long userId = getCurrentUserId();
        JiraConnection connection = jiraConnectionRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new RuntimeException("Connection not found"));

        boolean success;
        String message;

        try {
            String url = connection.getJiraBaseUrl() + "/rest/api/3/myself";
            String auth = connection.getJiraEmail() + ":" + connection.getJiraApiToken();
            String encodedAuth = Base64.getEncoder().encodeToString(auth.getBytes(StandardCharsets.UTF_8));

            HttpHeaders headers = new HttpHeaders();
            headers.set("Content-Type", "application/json");
            headers.set("Authorization", "Basic " + encodedAuth);
            HttpEntity<Void> entity = new HttpEntity<>(headers);

            restTemplate.exchange(url, HttpMethod.GET, entity, Map.class);

            success = true;
            message = "Connection successful";
        } catch (HttpClientErrorException e) {
            success = false;
            message = switch (e.getStatusCode().value()) {
                case 401 -> "Invalid Jira API Token. Please check your Jira API token.";
                case 403 -> "Jira authentication failed. Please check your email and API token.";
                case 404 -> "Invalid Jira URL. Please check your Jira Base URL.";
                default -> "Jira server returned error " + e.getStatusCode() + ".";
            };
        } catch (ResourceAccessException e) {
            success = false;
            message = "Jira server unavailable. Please check your network connection and Jira URL.";
        } catch (Exception e) {
            success = false;
            message = "Connection test failed: " + e.getMessage();
        }

        connection.setLastTestedAt(LocalDateTime.now());
        connection.setLastTestSuccess(success);
        connection.setLastTestMessage(message);

        connection = jiraConnectionRepository.save(connection);

        if (!success) {
            throw new RuntimeException(message);
        }

        return mapToDTO(connection);
    }

    @Transactional
    public JiraConnectionDTO setAsDefault(Long id) {
        Long userId = getCurrentUserId();
        JiraConnection connection = jiraConnectionRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new RuntimeException("Connection not found"));

        jiraConnectionRepository.findByUserIdAndIsDefaultTrue(userId)
                .ifPresent(conn -> {
                    conn.setIsDefault(false);
                    jiraConnectionRepository.save(conn);
                });

        connection.setIsDefault(true);
        connection = jiraConnectionRepository.save(connection);
        log.info("Set Jira connection {} as default for user {}", id, userId);

        return mapToDTO(connection);
    }

    @Transactional
    public JiraConnectionDTO toggleStatus(Long id) {
        Long userId = getCurrentUserId();
        JiraConnection connection = jiraConnectionRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new RuntimeException("Connection not found"));

        connection.setIsActive(!connection.getIsActive());
        connection = jiraConnectionRepository.save(connection);
        log.info("Toggled status for Jira connection {} to {}", id, connection.getIsActive());

        return mapToDTO(connection);
    }
    
    private JiraConnectionDTO mapToDTO(JiraConnection connection) {
        JiraConnectionDTO dto = new JiraConnectionDTO();
        dto.setId(connection.getId());
        dto.setConnectionName(connection.getConnectionName());
        dto.setJiraBaseUrl(connection.getJiraBaseUrl());
        dto.setJiraEmail(connection.getJiraEmail());
        dto.setJiraApiToken("********");
        dto.setIsDefault(connection.getIsDefault());
        dto.setIsActive(connection.getIsActive());
        dto.setLastTestedAt(connection.getLastTestedAt());
        dto.setLastTestSuccess(connection.getLastTestSuccess());
        dto.setLastTestMessage(connection.getLastTestMessage());
        dto.setCreatedAt(connection.getCreatedAt());
        dto.setUpdatedAt(connection.getUpdatedAt());
        return dto;
    }
}
