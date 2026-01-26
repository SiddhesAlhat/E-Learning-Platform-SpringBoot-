package com.elearning.repository;

import com.elearning.model.LearningGoal;
import com.elearning.model.LearningGoal.GoalStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LearningGoalRepository extends JpaRepository<LearningGoal, Long> {
    List<LearningGoal> findByUserId(Long userId);
    List<LearningGoal> findByUserIdAndStatus(Long userId, GoalStatus status);
    List<LearningGoal> findByStatus(GoalStatus status);
}
