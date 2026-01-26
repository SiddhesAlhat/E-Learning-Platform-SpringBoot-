package com.elearning.repository;

import com.elearning.model.LearningPath;
import com.elearning.model.LearningPath.PathStatus;
import com.elearning.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LearningPathRepository extends JpaRepository<LearningPath, Long> {
    Optional<LearningPath> findByStudent(User student); // Legacy method
    
    // New methods for enhanced learning path functionality
    List<LearningPath> findByUserId(Long userId);
    List<LearningPath> findByUserIdAndStatus(Long userId, PathStatus status);
    Optional<LearningPath> findByUserIdAndGoalId(Long userId, Long goalId);
    List<LearningPath> findByStatus(PathStatus status);
}
