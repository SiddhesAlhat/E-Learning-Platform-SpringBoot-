package com.elearning.controller;

import com.elearning.model.User;
import com.elearning.service.GamificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/gamification")
public class GamificationController {

    @Autowired
    private GamificationService gamificationService;

    @Autowired
    private com.elearning.repository.UserRepository userRepository;

    @GetMapping("/leaderboard")
    public ResponseEntity<List<Map<String, Object>>> getLeaderboard(
            @RequestParam(defaultValue = "10") int limit) {
        return ResponseEntity.ok(gamificationService.getGlobalLeaderboard(limit));
    }

    @GetMapping("/my-stats")
    public ResponseEntity<Map<String, Object>> getMyStats(
            @AuthenticationPrincipal UserDetails userDetails) {
        
        User user = userRepository.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));

        Map<String, Object> stats = new HashMap<>();
        stats.put("points", user.getPoints());
        stats.put("level", user.getLevel());
        stats.put("badges", user.getBadges());
        stats.put("rank", gamificationService.getUserRank(user));

        return ResponseEntity.ok(stats);
    }
}
