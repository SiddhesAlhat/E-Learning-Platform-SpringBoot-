package com.elearning.service;

import com.elearning.model.User;
import com.elearning.model.UserBadge;
import com.elearning.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class GamificationService {

    @Autowired
    private UserRepository userRepository;

    private static final int POINTS_PER_LESSON_COMPLETE = 10;
    private static final int POINTS_PER_QUIZ_PASS = 20;
    private static final int POINTS_PER_ASSIGNMENT_SUBMIT = 15;
    private static final int POINTS_PER_COURSE_COMPLETE = 100;

    /**
     * Award points to a user for an activity
     */
    public void awardPoints(User user, String activityType) {
        int points = 0;

        switch (activityType) {
            case "LESSON_COMPLETE":
                points = POINTS_PER_LESSON_COMPLETE;
                break;
            case "QUIZ_PASS":
                points = POINTS_PER_QUIZ_PASS;
                break;
            case "ASSIGNMENT_SUBMIT":
                points = POINTS_PER_ASSIGNMENT_SUBMIT;
                break;
            case "COURSE_COMPLETE":
                points = POINTS_PER_COURSE_COMPLETE;
                checkAndAwardBadge(user, "Course Master");
                break;
        }

        user.setPoints(user.getPoints() + points);
        updateLevel(user);
        userRepository.save(user);
    }

    /**
     * Update user level based on points
     */
    private void updateLevel(User user) {
        int points = user.getPoints();
        int newLevel = 1 + (points / 100); // 100 points per level

        if (newLevel > user.getLevel()) {
            user.setLevel(newLevel);
            checkAndAwardBadge(user, "Level " + newLevel);
        }
    }

    /**
     * Award a badge to a user
     */
    public void checkAndAwardBadge(User user, String badgeName) {
        // Check if user already has this badge
        if (user.getBadges() != null) {
            boolean hasBadge = user.getBadges().stream()
                    .anyMatch(badge -> badge.getBadgeName().equals(badgeName));
            if (hasBadge) return;
        }

        // Award new  badge
        UserBadge badge = new UserBadge();
        badge.setUser(user);
        badge.setBadgeName(badgeName);
        badge.setBadgeIconUrl("/badges/" + badgeName.toLowerCase().replace(" ", "_") + ".png");
        badge.setEarnedAt(LocalDateTime.now());

        if (user.getBadges() == null) {
            user.setBadges(new HashSet<>());
        }
        user.getBadges().add(badge);
        userRepository.save(user);
    }

    /**
     * Get global leaderboard
     */
    public List<Map<String, Object>> getGlobalLeaderboard(int limit) {
        return userRepository.findAll().stream()
                .sorted(Comparator.comparing(User::getPoints).reversed())
                .limit(limit)
                .map(this::mapUserToLeaderboardEntry)
                .collect(Collectors.toList());
    }

    /**
     * Get user's rank
     */
    public int getUserRank(User user) {
        List<User> allUsers = userRepository.findAll();
        allUsers.sort(Comparator.comparing(User::getPoints).reversed());

        for (int i = 0; i < allUsers.size(); i++) {
            if (allUsers.get(i).getId().equals(user.getId())) {
                return i + 1;
            }
        }
        return allUsers.size();
    }

    private Map<String, Object> mapUserToLeaderboardEntry(User user) {
        Map<String, Object> entry = new HashMap<>();
        entry.put("username", user.getUsername());
        entry.put("fullName", user.getFirstName() + " " + user.getLastName());
        entry.put("points", user.getPoints());
        entry.put("level", user.getLevel());
        entry.put("badgeCount", user.getBadges() != null ? user.getBadges().size() : 0);
        return entry;
    }
}
