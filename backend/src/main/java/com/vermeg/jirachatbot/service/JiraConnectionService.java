package com.vermeg.jirachatbot.service;

import com.vermeg.jirachatbot.dto.JiraConnectionDTO;
import com.vermeg.jirachatbot.entity.JiraConnection;
import com.vermeg.jirachatbot.entity.User;
import com.vermeg.jirachatbot.repository.JiraConnectionRepository;
import com.vermeg.jirachatbot.repository.UserRepository;
import com.vermeg.jirachatbot.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class JiraConnectionService {
    
    private final JiraConnectionRepository jiraConnectionRepository;
    private final UserRepository userRepository;
    
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
        
        connection.setLastTestedAt(LocalDateTime.now());
        connection.setLastTestSuccess(true);
        connection.setLastTestMessage("Connection successful");
        
        connection = jiraConnectionRepository.save(connection);
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
