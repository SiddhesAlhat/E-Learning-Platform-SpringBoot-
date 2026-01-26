package com.elearning.controller;

import com.elearning.dto.*;
import com.elearning.model.*;
import com.elearning.service.LearningPathService;
import com.elearning.service.UserService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/learning-paths")
@Slf4j
@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:3000"})
public class LearningPathController {

    @Autowired
    private LearningPathService learningPathService;

    @Autowired
    private UserService userService;

    // Generate personalized learning path
    @PostMapping("/generate")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<LearningPathDTO> generateLearningPath(
            @Valid @RequestBody CreateLearningGoalRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        User user = userService.findByUsername(userDetails.getUsername());
        
        LearningGoal goal = LearningGoal.builder()
                .user(user)
                .title(request.getTitle())
                .description(request.getDescription())
                .targetDate(request.getTargetDate())
                .build();
        
        LearningPath path = learningPathService.generatePersonalizedPath(user.getId(), goal);
        LearningPathDTO dto = learningPathService.getLearningPathWithProgress(user.getId(), path.getId());
        
        return ResponseEntity.status(HttpStatus.CREATED).body(dto);
    }

    // Get user's learning paths
    @GetMapping
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<List<LearningPathDTO>> getUserLearningPaths(
            @AuthenticationPrincipal UserDetails userDetails) {
        
        User user = userService.findByUsername(userDetails.getUsername());
        // This would need to be implemented in LearningPathService
        return ResponseEntity.ok(List.of());
    }

    // Get specific learning path with progress
    @GetMapping("/{pathId}")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<LearningPathDTO> getLearningPath(
            @PathVariable Long pathId,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        User user = userService.findByUsername(userDetails.getUsername());
        LearningPathDTO path = learningPathService.getLearningPathWithProgress(user.getId(), pathId);
        
        return ResponseEntity.ok(path);
    }

    // Update learning path based on progress
    @PutMapping("/{pathId}/update")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<LearningPathDTO> updateLearningPath(
            @PathVariable Long pathId,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        User user = userService.findByUsername(userDetails.getUsername());
        LearningPath path = learningPathService.updatePathBasedOnProgress(user.getId(), pathId);
        LearningPathDTO dto = learningPathService.getLearningPathWithProgress(user.getId(), pathId);
        
        return ResponseEntity.ok(dto);
    }

    // Mark course as completed in learning path
    @PostMapping("/{pathId}/courses/{courseId}/complete")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<Void> markCourseCompleted(
            @PathVariable Long pathId,
            @PathVariable Long courseId,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        User user = userService.findByUsername(userDetails.getUsername());
        learningPathService.markCourseAsCompleted(user.getId(), pathId, courseId);
        
        return ResponseEntity.ok().build();
    }

    // Get recommended next content
    @GetMapping("/recommendations/{courseId}")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<List<CourseDTO>> getNextContentRecommendations(
            @PathVariable Long courseId,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        User user = userService.findByUsername(userDetails.getUsername());
        List<Course> recommendations = learningPathService.recommendNextContent(user.getId(), courseId);
        
        // Convert to DTOs
        List<CourseDTO> dtos = recommendations.stream()
                .map(course -> new CourseDTO()) // Would need proper mapping
                .collect(java.util.stream.Collectors.toList());
        
        return ResponseEntity.ok(dtos);
    }

    // Create learning goal
    @PostMapping("/goals")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<LearningGoalDTO> createLearningGoal(
            @Valid @RequestBody CreateLearningGoalRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        User user = userService.findByUsername(userDetails.getUsername());
        
        LearningGoal goal = LearningGoal.builder()
                .user(user)
                .title(request.getTitle())
                .description(request.getDescription())
                .targetDate(request.getTargetDate())
                .build();
        
        // This would need to be saved via LearningGoalRepository
        LearningGoalDTO dto = new LearningGoalDTO();
        dto.setId(goal.getId());
        dto.setTitle(goal.getTitle());
        dto.setDescription(goal.getDescription());
        dto.setTargetDate(goal.getTargetDate());
        
        return ResponseEntity.status(HttpStatus.CREATED).body(dto);
    }

    // Get user's learning goals
    @GetMapping("/goals")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<List<LearningGoalDTO>> getUserLearningGoals(
            @AuthenticationPrincipal UserDetails userDetails) {
        
        User user = userService.findByUsername(userDetails.getUsername());
        // This would need to be implemented
        return ResponseEntity.ok(List.of());
    }

    // Get adaptive learning recommendations
    @GetMapping("/adaptive-recommendations")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<AdaptiveRecommendationsDTO> getAdaptiveRecommendations(
            @AuthenticationPrincipal UserDetails userDetails) {
        
        User user = userService.findByUsername(userDetails.getUsername());
        
        AdaptiveRecommendationsDTO recommendations = new AdaptiveRecommendationsDTO();
        recommendations.setUserId(user.getId());
        recommendations.setRecommendedCourses(List.of());
        recommendations.setWeakAreas(List.of());
        recommendations.setStudyTips(List.of());
        
        return ResponseEntity.ok(recommendations);
    }
}
