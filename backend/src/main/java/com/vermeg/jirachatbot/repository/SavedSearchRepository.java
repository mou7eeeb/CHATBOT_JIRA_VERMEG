package com.vermeg.jirachatbot.repository;

import com.vermeg.jirachatbot.entity.SavedSearch;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SavedSearchRepository extends JpaRepository<SavedSearch, Long> {
    
    List<SavedSearch> findByUserIdOrderByLastUsedAtDesc(Long userId);
    
    Optional<SavedSearch> findByIdAndUserId(Long id, Long userId);
}
