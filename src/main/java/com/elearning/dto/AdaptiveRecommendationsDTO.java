package com.elearning.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdaptiveRecommendationsDTO {
    private Long userId;
    private List<CourseDTO> recommendedCourses;
    private List<String> weakAreas;
    private List<String> studyTips;
    private String nextFocusArea;
    private Double confidenceScore;
}
