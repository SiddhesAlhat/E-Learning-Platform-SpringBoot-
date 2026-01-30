package com.elearning.dto;

import com.elearning.model.LearningPath;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LearningPathDTO {
    private Long id;
    private Long userId;
    private Long goalId;
    private String goalTitle;
    private LearningPath.PathStatus status;
    private LocalDate estimatedCompletionDate;
    private LocalDate actualCompletionDate;
    private LocalDate createdAt;
    private List<CourseDTO> courses;
    private Double overallProgress;
    private Integer totalCourses;
    private Integer completedCourses;
    private Integer currentCourseIndex;
}
