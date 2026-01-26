package com.elearning.repository;

import com.elearning.model.LearningPathCourse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LearningPathCourseRepository extends JpaRepository<LearningPathCourse, Long> {
    List<LearningPathCourse> findByLearningPathIdOrderBySequenceOrder(Long learningPathId);
    List<LearningPathCourse> findByCourseId(Long courseId);
    List<LearningPathCourse> findByLearningPathIdAndIsCompleted(Long learningPathId, Boolean isCompleted);
}
