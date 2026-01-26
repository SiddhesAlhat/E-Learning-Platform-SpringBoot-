package com.elearning.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.LocalDateTime;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "learning_analytics")
public class LearningAnalytics {
    @Id
    private String id;

    private Long userId;
    private Long courseId;
    
    private LocalDateTime lastActive;
    private Double completionPercentage;
    
    // Detailed metrics
    private Integer totalTimeSpentSeconds;
    private Integer videoWatchTimeSeconds;
    private Integer documentReadTimeSeconds;
    
    // Performance metrics
    private Double averageQuizScore;
    
    // Map of Lesson ID to time spent in seconds
    private Map<String, Integer> lessonTimeSpent;
    
    // Event log
    private java.util.List<AnalyticsEvent> events;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class AnalyticsEvent {
        private String type; // VIDEO_PLAY, QUIZ_START, etc.
        private LocalDateTime timestamp;
        private Map<String, Object> metadata;
    }
}
