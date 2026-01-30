package com.elearning.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ModuleDTO {
    
    private Long id;
    private String title;
    private String description;
    private Integer sequenceOrder;
    private Long courseId;
    private List<LessonDTO> lessons;
    private LocalDateTime createdAt;
    
    // Progress tracking
    private Integer totalLessons;
    private Integer completedLessons;
    private Double progressPercentage;
}
