package com.elearning.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents aggregated user profile data for ML predictions and learning path
 * generation
 * This is a data holder class, not a persistent entity
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserProfile {
    private User user;
    private LearningStyle learningStyle;
    private List<UserSkill> currentSkills = new ArrayList<>();
    private List<Long> completedCourseIds = new ArrayList<>();
    private Double availableTimePerWeek; // hours per week
    private Double averageHoursPerWeek; // actual study hours
    private Course.DifficultyLevel preferredDifficulty;
    private Double completionRate; // percentage
    private Double averageScore; // percentage
}
