package com.elearning.repository;

import com.elearning.model.LearningStyleProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface LearningStyleProfileRepository extends JpaRepository<LearningStyleProfile, Long> {
    Optional<LearningStyleProfile> findByUserId(Long userId);
    void deleteByUserId(Long userId);
}
