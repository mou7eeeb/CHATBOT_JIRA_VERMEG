package com.vermeg.jirachatbot.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "saved_searches")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SavedSearch {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "jira_connection_id")
    private JiraConnection jiraConnection;
    
    @Column(nullable = false)
    private String searchName;
    
    @Column(nullable = false, columnDefinition = "TEXT")
    private String query;
    
    @Column(nullable = false, columnDefinition = "TEXT")
    private String jql;
    
    @Column(columnDefinition = "TEXT")
    private String description;
    
    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;
    
    private LocalDateTime lastUsedAt;
    
    @Builder.Default
    private Integer usageCount = 0;
}
