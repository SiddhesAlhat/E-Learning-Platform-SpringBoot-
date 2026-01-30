package com.elearning.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CourseAnalyticsDTO {
    private Long courseId;
    private String courseTitle;
    private Integer totalEnrollments;
    private Integer activeEnrollments;
    private Integer completedEnrollments;
    private Double averageCompletionRate;
    private Double averageProgress;
    private Integer totalRevenue;
    private LocalDateTime lastUpdated;
    
    // Engagement metrics
    private Double averageTimeSpent;
    private Integer totalLessonsCompleted;
    private Double averageRating;
    private Integer dropouts;
}
