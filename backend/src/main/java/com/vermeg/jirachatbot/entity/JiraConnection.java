package com.vermeg.jirachatbot.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "jira_connections")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JiraConnection {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @JsonIgnore
    private User user;
    
    @Column(nullable = false)
    private String connectionName;
    
    @Column(nullable = false)
    private String jiraBaseUrl;
    
    @Column(nullable = false)
    private String jiraEmail;
    
    @Column(nullable = false)
    private String jiraApiToken;
    
    @Column(nullable = false)
    @Builder.Default
    private Boolean isDefault = false;
    
    @Column(nullable = false)
    @Builder.Default
    private Boolean isActive = true;
    
    private LocalDateTime lastTestedAt;
    
    private Boolean lastTestSuccess;
    
    private String lastTestMessage;
    
    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;
}
