package com.elearning.dto;

import com.elearning.model.LearningGoal;
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
public class LearningGoalDTO {
    private Long id;
    private String title;
    private String description;
    private LocalDate targetDate;
    private LearningGoal.GoalStatus status;
    private LocalDate createdAt;
    private LocalDate completedAt;
    private List<Long> learningPathIds;
    private Double progressPercentage;
}
