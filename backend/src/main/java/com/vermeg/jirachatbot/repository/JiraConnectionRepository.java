package com.vermeg.jirachatbot.repository;

import com.vermeg.jirachatbot.entity.JiraConnection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface JiraConnectionRepository extends JpaRepository<JiraConnection, Long> {
    
    List<JiraConnection> findByUserIdAndIsActiveTrue(Long userId);
    
    List<JiraConnection> findByUserId(Long userId);
    
    Optional<JiraConnection> findByUserIdAndIsDefaultTrue(Long userId);
    
    Optional<JiraConnection> findByIdAndUserId(Long id, Long userId);
}
