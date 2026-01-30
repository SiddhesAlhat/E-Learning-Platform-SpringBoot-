package com.elearning.dto;

import com.elearning.model.Course;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CourseDetailDTO extends CourseDTO {
    
    private String instructorName;
    private String categoryName;
    private Integer totalModules;
    private Integer totalLessons;
    private Integer totalDuration; // in minutes
    private Double userProgress; // percentage
    private Boolean enrolled;
    private List<ModuleDTO> modules;
    private List<String> prerequisites;
    private LocalDateTime lastUpdated;
    
    // Additional analytics
    private Integer enrolledCount;
    private Double averageRating;
    private Integer completionRate;
}
