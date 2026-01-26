package com.elearning.repository;

import com.elearning.model.LearningAnalytics;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.List;
import java.util.Optional;

public interface LearningAnalyticsRepository extends MongoRepository<LearningAnalytics, String> {
    List<LearningAnalytics> findByUserId(Long userId);
    Optional<LearningAnalytics> findByUserIdAndCourseId(Long userId, Long courseId);
}
