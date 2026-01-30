package com.elearning.dto;

import com.elearning.model.Course;
import com.elearning.model.LearningStyle;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Response object from ML model predictions
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MLPredictionResponse {
    // Course recommendations
    private List<Course> recommendedCourses;

    // Predictions
    private Double successProbability;
    private LearningStyle predictedLearningStyle;
    private Double recommendedStudyHours;
    private Double dropoutRisk;

    // Analysis results
    private List<String> weakAreas;
    private List<String> personalizedTips;

    // Metadata
    private String modelVersion;
    private Double confidenceScore;
}
