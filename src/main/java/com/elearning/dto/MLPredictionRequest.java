package com.elearning.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Request object for ML model predictions
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MLPredictionRequest {
    // User context
    private Long userId;
    private Long courseId;
    private Long goalId;
    private String goalTitle;

    // Learning profile
    private String learningStyle;
    private Double availableTimePerWeek;
    private String preferredDifficulty;
    private List<Long> completedCourses;
    private List<String> skillGaps;

    // Behavior data for learning style detection
    private Double videoWatchTime;
    private Double readingTime;
    private Double interactiveExerciseTime;
    private Double quizPerformance;
    private Integer forumParticipation;
}
