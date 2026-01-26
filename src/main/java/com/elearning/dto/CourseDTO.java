package com.elearning.dto;

import lombok.Data;
import java.util.List;

@Data
public class CourseDTO {
    private Long id;
    private String title;
    private String description;
    private String thumbnailUrl;
    private String difficultyLevel;
    private String category;
    private String instructorName; // Mapped from instructor.firstName + lastName
    private List<String> tags;
}
