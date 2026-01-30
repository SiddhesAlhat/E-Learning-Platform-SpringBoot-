package com.elearning.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Future;
import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateLearningGoalRequest {
    @NotBlank(message = "Goal title is required")
    private String title;
    
    private String description;
    
    @Future(message = "Target date must be in the future")
    private LocalDate targetDate;
    
    private List<String> skillAreas;
    private List<Long> preferredCourseIds;
}
