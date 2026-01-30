package com.elearning.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents performance metrics for a user in a specific course
 * Used for adaptive content recommendations
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PerformanceMetrics {
    private Long userId;
    private Long courseId;
    private Double averageScore; // Overall average score percentage
    private Integer completedLessons;
    private Integer totalLessons;
    private Long lastCompletedLesson;
    private List<String> weakTopics = new ArrayList<>(); // Topics where score < 70%
    private List<String> strongTopics = new ArrayList<>(); // Topics where score > 85%
    private Double engagementScore; // Based on time spent, interactions, etc.
    private Integer consecutiveFailures; // Number of consecutive low scores
}
