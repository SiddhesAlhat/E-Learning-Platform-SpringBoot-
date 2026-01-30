package com.elearning.service;

import com.elearning.dto.LearningPathDTO;
import com.elearning.model.*;
import com.elearning.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class LearningPathService {

    @Autowired
    private LearningPathRepository learningPathRepository;

    @Autowired
    private LearningGoalRepository learningGoalRepository;

    @Autowired
    private LearningPathCourseRepository learningPathCourseRepository;

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private UserSkillRepository userSkillRepository;

    @Autowired
    private LearningStyleProfileRepository learningStyleProfileRepository;

    @Autowired
    private MLModelService mlModelService;

    @Autowired
    private UserAnalyticsService userAnalyticsService;

    @Value("${learning.path.default.duration.weeks:12}")
    private Integer defaultDurationWeeks;

    public LearningPath generatePersonalizedPath(Long userId, LearningGoal goal) {
        // 1. Get user's current skill level and learning history
        UserProfile profile = userAnalyticsService.getUserProfile(userId);

        // 2. Get target skills required for the goal
        List<Skill> targetSkills = getSkillsForGoal(goal);

        // 3. Find skill gaps
        List<Skill> skillGaps = findSkillGaps(profile.getCurrentSkills(), targetSkills);

        // 4. Use ML model to predict optimal course sequence
        List<Course> recommendedCourses = mlModelService.predictOptimalPath(
                profile,
                skillGaps,
                goal);

        // 5. Ensure prerequisites are met using topological sort
        List<Course> orderedCourses = orderCoursesWithPrerequisites(recommendedCourses);

        // 6. Estimate time to completion based on user's learning pace
        Duration estimatedTime = estimateCompletionTime(profile, orderedCourses);

        // 7. Create and save learning path
        LearningPath path = LearningPath.builder()
                .user(profile.getUser())
                .goal(goal)
                .status(LearningPath.PathStatus.ACTIVE)
                .estimatedCompletionDate(LocalDate.now().plusDays(estimatedTime.toDays()))
                .build();

        path = learningPathRepository.save(path);

        // 8. Add courses to path with sequence order
        for (int i = 0; i < orderedCourses.size(); i++) {
            LearningPathCourse pathCourse = LearningPathCourse.builder()
                    .learningPath(path)
                    .course(orderedCourses.get(i))
                    .sequenceOrder(i + 1)
                    .build();
            learningPathCourseRepository.save(pathCourse);
            path.getCourses().add(pathCourse);
        }

        return path;
    }

    public LearningPath updatePathBasedOnProgress(Long userId, Long pathId) {
        LearningPath path = learningPathRepository.findById(pathId)
                .orElseThrow(() -> new RuntimeException("Learning path not found"));

        UserProfile profile = userAnalyticsService.getUserProfile(userId);
        UserProgress progress = userAnalyticsService.getProgress(userId);

        if (progress.isBehindSchedule()) {
            // Adjust path: suggest more intensive study or remove optional courses
            adjustPathForSlowProgress(path, profile);
        } else if (progress.isAheadOfSchedule()) {
            // Suggest additional advanced courses
            enrichPathWithAdvancedContent(path, profile);
        }

        // Check if learning style changed
        LearningStyle currentStyle = mlModelService.detectLearningStyle(userId);
        LearningStyleProfile profileStyle = learningStyleProfileRepository
                .findByUserId(userId).orElse(null);

        if (profileStyle == null || !currentStyle.equals(profileStyle.getDominantStyle())) {
            // Update content recommendations based on new learning style
            updateContentRecommendations(path, currentStyle);
        }

        return learningPathRepository.save(path);
    }

    public List<Course> recommendNextContent(Long userId, Long courseId) {
        // Analyze recent quiz/assignment scores
        PerformanceMetrics metrics = userAnalyticsService.getRecentPerformance(userId, courseId);

        if (metrics.getAverageScore() < 60) {
            // Struggling - recommend easier content, review materials
            return courseRepository.findReviewContent(courseId, metrics.getWeakTopics());
        } else if (metrics.getAverageScore() > 85) {
            // Excelling - recommend advanced content, skip basics
            return courseRepository.findAdvancedContent(courseId);
        } else {
            // On track - standard progression
            return courseRepository.findNextContent(courseId, metrics.getLastCompletedLesson());
        }
    }

    public LearningPathDTO getLearningPathWithProgress(Long userId, Long pathId) {
        LearningPath path = learningPathRepository.findById(pathId)
                .orElseThrow(() -> new RuntimeException("Learning path not found"));

        // Calculate progress
        List<LearningPathCourse> pathCourses = learningPathCourseRepository
                .findByLearningPathIdOrderBySequenceOrder(pathId);

        int completedCourses = (int) pathCourses.stream()
                .filter(LearningPathCourse::getIsCompleted)
                .count();

        double progressPercentage = pathCourses.isEmpty() ? 0.0 : (double) completedCourses / pathCourses.size() * 100;

        LearningPathDTO dto = new LearningPathDTO();
        dto.setId(path.getId());
        dto.setGoalTitle(path.getGoal().getTitle());
        dto.setStatus(path.getStatus());
        dto.setProgressPercentage(progressPercentage);
        dto.setEstimatedCompletionDate(path.getEstimatedCompletionDate());
        dto.setCourses(pathCourses.stream()
                .map(pc -> mapToCourseDTO(pc.getCourse(), pc.getSequenceOrder(), pc.getIsCompleted()))
                .collect(Collectors.toList()));

        return dto;
    }

    public void markCourseAsCompleted(Long userId, Long pathId, Long courseId) {
        LearningPathCourse pathCourse = learningPathCourseRepository
                .findByLearningPathIdAndCourseId(pathId, courseId)
                .orElseThrow(() -> new RuntimeException("Course not found in learning path"));

        pathCourse.setIsCompleted(true);
        pathCourse.setCompletedAt(java.time.LocalDateTime.now());
        learningPathCourseRepository.save(pathCourse);

        // Check if entire path is completed
        LearningPath path = pathCourse.getLearningPath();
        List<LearningPathCourse> allCourses = learningPathCourseRepository
                .findByLearningPathIdOrderBySequenceOrder(pathId);

        boolean allCompleted = allCourses.stream().allMatch(LearningPathCourse::getIsCompleted);
        if (allCompleted) {
            path.setStatus(LearningPath.PathStatus.COMPLETED);
            path.setActualCompletionDate(LocalDate.now());
            learningPathRepository.save(path);
        }
    }

    private List<Skill> getSkillsForGoal(LearningGoal goal) {
        // This would typically query a goal-to-skills mapping
        // For now, return a placeholder implementation
        return Arrays.asList(
                new Skill("Java Programming", Skill.ProficiencyLevel.INTERMEDIATE),
                new Skill("Spring Framework", Skill.ProficiencyLevel.INTERMEDIATE));
    }

    private List<Skill> findSkillGaps(List<UserSkill> currentSkills, List<Skill> targetSkills) {
        Map<String, UserSkill> currentSkillMap = currentSkills.stream()
                .collect(Collectors.toMap(UserSkill::getSkillName, skill -> skill));

        return targetSkills.stream()
                .filter(target -> {
                    UserSkill current = currentSkillMap.get(target.getName());
                    return current == null ||
                            current.getProficiencyLevel().ordinal() < target.getLevel().ordinal();
                })
                .collect(Collectors.toList());
    }

    private List<Course> orderCoursesWithPrerequisites(List<Course> courses) {
        // Topological sort implementation
        Map<Long, Course> courseMap = courses.stream()
                .collect(Collectors.toMap(Course::getId, course -> course));

        Map<Long, Integer> inDegree = new HashMap<>();
        Map<Long, List<Long>> adjacencyList = new HashMap<>();

        // Initialize graphs
        for (Course course : courses) {
            inDegree.put(course.getId(), 0);
            adjacencyList.put(course.getId(), new ArrayList<>());
        }

        // Build prerequisite graph
        for (Course course : courses) {
            for (CoursePrerequisite prereq : course.getPrerequisites()) {
                if (courseMap.containsKey(prereq.getPrerequisiteCourse().getId())) {
                    adjacencyList.get(prereq.getPrerequisiteCourse().getId()).add(course.getId());
                    inDegree.put(course.getId(), inDegree.get(course.getId()) + 1);
                }
            }
        }

        // Topological sort using Kahn's algorithm
        Queue<Long> queue = new LinkedList<>();
        for (Map.Entry<Long, Integer> entry : inDegree.entrySet()) {
            if (entry.getValue() == 0) {
                queue.offer(entry.getKey());
            }
        }

        List<Course> result = new ArrayList<>();
        while (!queue.isEmpty()) {
            Long courseId = queue.poll();
            result.add(courseMap.get(courseId));

            for (Long neighbor : adjacencyList.get(courseId)) {
                inDegree.put(neighbor, inDegree.get(neighbor) - 1);
                if (inDegree.get(neighbor) == 0) {
                    queue.offer(neighbor);
                }
            }
        }

        return result;
    }

    private Duration estimateCompletionTime(UserProfile profile, List<Course> courses) {
        // Estimate based on user's learning pace and course difficulties
        double averageHoursPerCourse = profile.getAverageHoursPerWeek() * 4; // per month
        int totalEstimatedHours = courses.stream()
                .mapToInt(course -> course.getEstimatedDuration() != null ? course.getEstimatedDuration() : 40)
                .sum();

        int weeksNeeded = (int) Math.ceil(totalEstimatedHours / (averageHoursPerCourse / 4));
        return Duration.ofDays(weeksNeeded * 7L);
    }

    private void adjustPathForSlowProgress(LearningPath path, UserProfile profile) {
        // Remove optional courses, suggest more study time
        List<LearningPathCourse> courses = learningPathCourseRepository
                .findByLearningPathIdOrderBySequenceOrder(path.getId());

        List<LearningPathCourse> optionalCourses = courses.stream()
                .filter(pc -> !pc.getCourse().getIsPublished() ||
                        pc.getCourse().getDifficultyLevel() == Course.DifficultyLevel.ADVANCED)
                .collect(Collectors.toList());

        // Remove optional courses
        optionalCourses.forEach(learningPathCourseRepository::delete);

        // Update estimated completion date
        path.setEstimatedCompletionDate(LocalDate.now().plusWeeks(defaultDurationWeeks));
    }

    private void enrichPathWithAdvancedContent(LearningPath path, UserProfile profile) {
        // Add advanced courses to the path
        List<Course> advancedCourses = courseRepository.findAdvancedContentForUser(
                path.getUser().getId(),
                path.getGoal().getId());

        int nextSequence = learningPathCourseRepository
                .findByLearningPathIdOrderBySequenceOrder(path.getId()).size() + 1;

        for (Course course : advancedCourses) {
            LearningPathCourse pathCourse = LearningPathCourse.builder()
                    .learningPath(path)
                    .course(course)
                    .sequenceOrder(nextSequence++)
                    .build();
            learningPathCourseRepository.save(pathCourse);
        }
    }

    private void updateContentRecommendations(LearningPath path, LearningStyle learningStyle) {
        // Update content recommendations based on learning style
        // This would involve filtering/reordering courses based on content type
        // preferences
    }

    // Helper classes (would normally be in separate files)
    public static class Skill {
        private String name;
        private ProficiencyLevel level;

        public Skill(String name, ProficiencyLevel level) {
            this.name = name;
            this.level = level;
        }

        public String getName() {
            return name;
        }

        public ProficiencyLevel getLevel() {
            return level;
        }

        public enum ProficiencyLevel {
            BEGINNER, INTERMEDIATE, ADVANCED, EXPERT
        }
    }
}
