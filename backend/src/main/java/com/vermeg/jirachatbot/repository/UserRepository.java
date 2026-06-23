package com.vermeg.jirachatbot.repository;

import com.vermeg.jirachatbot.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    
    Optional<User> findByEmail(String email);
    
    Boolean existsByEmail(String email);
    
    long countByEnabledTrue();
    
    long countByRole(User.Role role);
    
    long countByCreatedAtAfter(LocalDateTime date);
}
