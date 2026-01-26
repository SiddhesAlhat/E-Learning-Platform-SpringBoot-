package com.elearning.service;

import com.elearning.model.*;
import com.elearning.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class AdaptiveLearningService {

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private SubmissionRepository submissionRepository;

    @Autowired
    private LearningPathRepository learningPathRepository;

    @Autowired
    private LearningAnalyticsRepository learningAnalyticsRepository;

    /**
     * Generate personalized course recommendations for a student
     * Uses collaborative filtering and content-based filtering
     */
    public List<Course> getPersonalizedRecommendations(User student, int limit) {
        // Get student's performance data
        List<Submission> userSubmissions = submissionRepository.findByStudent(student);
        List<LearningAnalytics> analytics = learningAnalyticsRepository.findByUserId(student.getId());

        // Analyze student's strengths and weaknesses
        Map<String, Double> categoryPerformance = analyzeCategoryPerformance(userSubmissions, analytics);

        // Get all available courses
        List<Course> allCourses = courseRepository.findByIsPublishedTrue();

        // Score courses based on relevance
        Map<Course, Double> courseScores = new HashMap<>();
        for (Course course : allCourses) {
            double score = calculateCourseRelevance(course, student, categoryPerformance, analytics);
            courseScores.put(course, score);
        }

        // Sort courses by score and return top N
        return courseScores.entrySet().stream()
                .sorted(Map.Entry.<Course, Double>comparingByValue().reversed())
                .limit(limit)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }

    private Map<String, Double> analyzeCategoryPerformance(List<Submission> submissions, List<LearningAnalytics> analytics) {
        Map<String, Double> performance = new HashMap<>();

        // Calculate average scores by category (simplified)
        for (Submission sub : submissions) {
            if (sub.isGraded() && sub.getAssignment() != null) {
                String category = getCourseCategory(sub.getAssignment());
                double score = (double) sub.getScore() / sub.getAssignment().getMaxScore();
                
                performance.merge(category, score, (old, newVal) -> (old + newVal) / 2);
            }
        }

        return performance;
    }

    private String getCourseCategory(Assignment assignment) {
        // Navigate up to get course category
        if (assignment.getLesson() != null && 
            assignment.getLesson().getModule() != null && 
            assignment.getLesson().getModule().getCourse() != null) {
            return assignment.getLesson().getModule().getCourse().getCategory();
        }
        return "General";
    }

    private double calculateCourseRelevance(Course course, User student, 
                                           Map<String, Double> categoryPerformance,
                                           List<LearningAnalytics> analytics) {
        double score = 0.0;

        // Factor 1: Category interest (30%)
        String category = course.getCategory();
        if (categoryPerformance.containsKey(category)) {
            // Recommend categories where student performed well
            score += categoryPerformance.get(category) * 0.3;
        }

        // Factor 2: Difficulty matching (25%)
        String difficulty = course.getDifficultyLevel();
        double difficultyScore = matchDifficultyToStudent(difficulty, student, categoryPerformance);
        score += difficultyScore * 0.25;

        // Factor 3: Completion rate boost (20%)
        // If student has good completion rates, recommend similar courses
        double avgCompletion = analytics.stream()
                .mapToDouble(LearningAnalytics::getCompletionPercentage)
                .average()
                .orElse(0.5);
        score += avgCompletion * 0.2;

        // Factor 4: Tag similarity (15%)
        double tagSimilarity = calculateTagSimilarity(course, analytics);
        score += tagSimilarity * 0.15;

        // Factor 5: Popularity / Quality (10%)
        score += 0.1; // Simplified - could use ratings/enrollments

        return score;
    }

    private double matchDifficultyToStudent(String courseDifficulty, User student, 
                                           Map<String, Double> performance) {
        // Calculate student's overall performance level
        double avgPerformance = performance.values().stream()
                .mapToDouble(Double::doubleValue)
                .average()
                .orElse(0.5);

        // Match difficulty to performance
        if (courseDifficulty == null) return 0.5;

        switch (courseDifficulty.toUpperCase()) {
            case "BEGINNER":
                return avgPerformance < 0.6 ? 1.0 : 0.5;
            case "INTERMEDIATE":
                return (avgPerformance >= 0.5 && avgPerformance <= 0.8) ? 1.0 : 0.6;
            case "ADVANCED":
                return avgPerformance > 0.7 ? 1.0 : 0.3;
            default:
                return 0.5;
        }
    }

    private double calculateTagSimilarity(Course course, List<LearningAnalytics> analytics) {
        // Compare course tags with previously studied course tags
        if (course.getTags() == null || course.getTags().isEmpty()) {
            return 0.5;
        }

        // Simplified version - in production, analyze tags from completed courses
        return 0.5;
    }

    /**
     * Update learning path for a student
     */
    public LearningPath updateLearningPath(User student) {
        LearningPath path = learningPathRepository.findByStudent(student)
                .orElse(new LearningPath());

        path.setStudent(student);

        // Get top 10 recommended courses
        List<Course> recommendations = getPersonalizedRecommendations(student, 10);
        List<Long> courseIds = recommendations.stream()
                .map(Course::getId)
                .collect(Collectors.toList());

        path.setRecommendedCourseIds(courseIds);

        // Calculate overall progress
        List<LearningAnalytics> analytics = learningAnalyticsRepository.findByUserId(student.getId());
        double avgProgress = analytics.stream()
                .mapToDouble(LearningAnalytics::getCompletionPercentage)
                .average()
                .orElse(0.0);
        path.setProgress(avgProgress);

        // Determine focus area
        Map<String, Double> categoryPerformance = analyzeCategoryPerformance(
                submissionRepository.findByStudent(student), analytics);
        
        String focusArea = categoryPerformance.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse("Exploring");

        path.setCurrentFocusArea(focusArea);

        return learningPathRepository.save(path);
    }
}
