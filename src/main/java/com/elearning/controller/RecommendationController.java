package com.elearning.controller;

import com.elearning.model.Course;
import com.elearning.model.User;
import com.elearning.service.AdaptiveLearningService;
import com.elearning.service.GamificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/recommendations")
public class RecommendationController {

    @Autowired
    private AdaptiveLearningService adaptiveLearningService;

    @Autowired
    private com.elearning.repository.UserRepository userRepository;

    @GetMapping
    public ResponseEntity<List<Course>> getRecommendations(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(defaultValue = "10") int limit) {
        
        User student = userRepository.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<Course> recommendations = adaptiveLearningService.getPersonalizedRecommendations(student, limit);
        return ResponseEntity.ok(recommendations);
    }

    @PostMapping("/update-path")
    public ResponseEntity<?> updateLearningPath(@AuthenticationPrincipal UserDetails userDetails) {
        User student = userRepository.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));

        adaptiveLearningService.updateLearningPath(student);
        return ResponseEntity.ok("Learning path updated successfully");
    }
}
