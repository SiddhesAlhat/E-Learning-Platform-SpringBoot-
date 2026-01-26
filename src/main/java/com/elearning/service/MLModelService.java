package com.elearning.service;

import com.elearning.dto.MLPredictionRequest;
import com.elearning.dto.MLPredictionResponse;
import com.elearning.model.Course;
import com.elearning.model.LearningGoal;
import com.elearning.model.LearningStyle;
import com.elearning.model.UserProfile;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.List;

@Service
public class MLModelService {

    @Autowired
    private RestTemplate restTemplate;

    @Value("${ml.service.url:http://localhost:5000}")
    private String mlServiceUrl;

    @Value("${ml.service.timeout:30000}")
    private int timeout;

    public List<Course> predictOptimalPath(UserProfile profile, List<LearningPathService.Skill> skillGaps, LearningGoal goal) {
        MLPredictionRequest request = new MLPredictionRequest();
        request.setUserId(profile.getUser().getId());
        request.setSkillGaps(convertSkillGaps(skillGaps));
        request.setGoalId(goal.getId());
        request.setGoalTitle(goal.getTitle());
        request.setLearningStyle(profile.getLearningStyle());
        request.setAvailableTimePerWeek(profile.getAvailableTimePerWeek());
        request.setPreferredDifficulty(profile.getPreferredDifficulty());
        request.setCompletedCourses(profile.getCompletedCourseIds());

        try {
            MLPredictionResponse response = restTemplate.postForObject(
                mlServiceUrl + "/api/predict/learning-path",
                request,
                MLPredictionResponse.class
            );

            if (response != null && response.getRecommendedCourses() != null) {
                return response.getRecommendedCourses();
            }
        } catch (Exception e) {
            // Fallback to simple recommendation if ML service is unavailable
            return getFallbackRecommendations(skillGaps, profile);
        }

        return getFallbackRecommendations(skillGaps, profile);
    }

    public double predictCourseSuccess(Long userId, Long courseId) {
        try {
            MLPredictionRequest request = new MLPredictionRequest();
            request.setUserId(userId);
            request.setCourseId(courseId);

            MLPredictionResponse response = restTemplate.postForObject(
                mlServiceUrl + "/api/predict/course-success",
                request,
                MLPredictionResponse.class
            );

            return response != null ? response.getSuccessProbability() : 0.5;
        } catch (Exception e) {
            return 0.5; // Default probability
        }
    }

    public LearningStyle detectLearningStyle(Long userId) {
        try {
            UserBehaviorData behavior = getUserBehaviorData(userId);

            MLPredictionRequest request = new MLPredictionRequest();
            request.setUserId(userId);
            request.setVideoWatchTime(behavior.getVideoWatchTime());
            request.setReadingTime(behavior.getReadingTime());
            request.setInteractiveExerciseTime(behavior.getInteractiveExerciseTime());
            request.setQuizPerformance(behavior.getQuizPerformance());
            request.setForumParticipation(behavior.getForumParticipation());

            MLPredictionResponse response = restTemplate.postForObject(
                mlServiceUrl + "/api/predict/learning-style",
                request,
                MLPredictionResponse.class
            );

            return response != null ? response.getPredictedLearningStyle() : LearningStyle.MULTIMODAL;
        } catch (Exception e) {
            return LearningStyle.MULTIMODAL;
        }
    }

    public List<Course> recommendSimilarCourses(Long courseId, Long userId) {
        try {
            MLPredictionRequest request = new MLPredictionRequest();
            request.setUserId(userId);
            request.setCourseId(courseId);

            MLPredictionResponse response = restTemplate.postForObject(
                mlServiceUrl + "/api/recommend/similar-courses",
                request,
                MLPredictionResponse.class
            );

            return response != null ? response.getRecommendedCourses() : List.of();
        } catch (Exception e) {
            return List.of();
        }
    }

    public double predictOptimalStudyTime(Long userId, Long courseId) {
        try {
            MLPredictionRequest request = new MLPredictionRequest();
            request.setUserId(userId);
            request.setCourseId(courseId);

            MLPredictionResponse response = restTemplate.postForObject(
                mlServiceUrl + "/api/predict/study-time",
                request,
                MLPredictionResponse.class
            );

            return response != null ? response.getRecommendedStudyHours() : 5.0;
        } catch (Exception e) {
            return 5.0; // Default 5 hours per week
        }
    }

    public List<String> identifyWeakAreas(Long userId, Long courseId) {
        try {
            MLPredictionRequest request = new MLPredictionRequest();
            request.setUserId(userId);
            request.setCourseId(courseId);

            MLPredictionResponse response = restTemplate.postForObject(
                mlServiceUrl + "/api/analyze/weak-areas",
                request,
                MLPredictionResponse.class
            );

            return response != null ? response.getWeakAreas() : List.of();
        } catch (Exception e) {
            return List.of();
        }
    }

    public boolean isAtRiskOfDroppingOut(Long userId) {
        try {
            MLPredictionRequest request = new MLPredictionRequest();
            request.setUserId(userId);

            MLPredictionResponse response = restTemplate.postForObject(
                mlServiceUrl + "/api/predict/dropout-risk",
                request,
                MLPredictionResponse.class
            );

            return response != null && response.getDropoutRisk() > 0.7;
        } catch (Exception e) {
            return false;
        }
    }

    public List<String> generatePersonalizedTips(Long userId, Long courseId) {
        try {
            MLPredictionRequest request = new MLPredictionRequest();
            request.setUserId(userId);
            request.setCourseId(courseId);

            MLPredictionResponse response = restTemplate.postForObject(
                mlServiceUrl + "/api/generate/tips",
                request,
                MLPredictionResponse.class
            );

            return response != null ? response.getPersonalizedTips() : getDefaultTips();
        } catch (Exception e) {
            return getDefaultTips();
        }
    }

    private List<Course> getFallbackRecommendations(List<LearningPathService.Skill> skillGaps, UserProfile profile) {
        // Simple fallback logic based on skill gaps and user preferences
        // This would query the database for courses matching the skill gaps
        return List.of(); // Placeholder
    }

    private List<String> convertSkillGaps(List<LearningPathService.Skill> skillGaps) {
        return skillGaps.stream()
                .map(skill -> skill.getName() + ":" + skill.getLevel())
                .collect(java.util.stream.Collectors.toList());
    }

    private UserBehaviorData getUserBehaviorData(Long userId) {
        // This would aggregate user behavior from various sources
        // For now, return placeholder data
        UserBehaviorData data = new UserBehaviorData();
        data.setVideoWatchTime(120.5); // minutes
        data.setReadingTime(80.0);
        data.setInteractiveExerciseTime(45.0);
        data.setQuizPerformance(85.0);
        data.setForumParticipation(15);
        return data;
    }

    private List<String> getDefaultTips() {
        return Arrays.asList(
            "Take regular breaks to maintain focus",
            "Review difficult concepts before moving on",
            "Practice with hands-on exercises",
            "Join study groups for collaborative learning"
        );
    }

    // Inner class for user behavior data
    private static class UserBehaviorData {
        private double videoWatchTime;
        private double readingTime;
        private double interactiveExerciseTime;
        private double quizPerformance;
        private int forumParticipation;

        // Getters and setters
        public double getVideoWatchTime() { return videoWatchTime; }
        public void setVideoWatchTime(double videoWatchTime) { this.videoWatchTime = videoWatchTime; }
        public double getReadingTime() { return readingTime; }
        public void setReadingTime(double readingTime) { this.readingTime = readingTime; }
        public double getInteractiveExerciseTime() { return interactiveExerciseTime; }
        public void setInteractiveExerciseTime(double interactiveExerciseTime) { this.interactiveExerciseTime = interactiveExerciseTime; }
        public double getQuizPerformance() { return quizPerformance; }
        public void setQuizPerformance(double quizPerformance) { this.quizPerformance = quizPerformance; }
        public int getForumParticipation() { return forumParticipation; }
        public void setForumParticipation(int forumParticipation) { this.forumParticipation = forumParticipation; }
    }
}
