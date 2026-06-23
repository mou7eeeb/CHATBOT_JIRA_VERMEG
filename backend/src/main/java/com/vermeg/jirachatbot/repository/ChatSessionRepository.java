package com.vermeg.jirachatbot.repository;

import com.vermeg.jirachatbot.entity.ChatSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ChatSessionRepository extends JpaRepository<ChatSession, Long> {
    
    List<ChatSession> findByUserIdOrderByUpdatedAtDesc(Long userId);
    
    List<ChatSession> findByUserIdAndIsActiveTrueOrderByUpdatedAtDesc(Long userId);
    
    Optional<ChatSession> findByIdAndUserId(Long id, Long userId);
}
