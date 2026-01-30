package com.elearning.repository;

import com.elearning.model.Course;
import com.elearning.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CourseRepository extends JpaRepository<Course, Long> {

    List<Course> findByInstructor(User instructor);

    List<Course> findByInstructorId(Long instructorId);

    List<Course> findByCategory(String category);

    List<Course> findByCategoryId(Long categoryId);

    List<Course> findByIsPublishedTrue();

    Page<Course> findByIsPublishedTrue(Pageable pageable);

    // Additional methods for enhanced functionality
    @Query("SELECT c FROM Course c WHERE c.isPublished = true AND c.title LIKE %:query% OR c.description LIKE %:query%")
    Page<Course> searchPublishedCourses(@Param("query") String query, Pageable pageable);

    @Query("SELECT c FROM Course c JOIN c.tags t WHERE t IN :tags AND c.isPublished = true")
    List<Course> findByTags(@Param("tags") List<String> tags);

    @Query("SELECT c FROM Course c WHERE c.difficultyLevel = :difficulty AND c.isPublished = true")
    List<Course> findByDifficultyLevel(@Param("difficulty") Course.DifficultyLevel difficulty);

    @Query("SELECT c FROM Course c WHERE c.price = 0 AND c.isPublished = true")
    List<Course> findFreeCourses();

    @Query("SELECT c FROM Course c WHERE c.price > 0 AND c.isPublished = true")
    List<Course> findPaidCourses();

    // Adaptive learning methods - placeholder implementations
    // TODO: Implement proper query logic based on weak topics and learning patterns
    @Query("SELECT c FROM Course c WHERE c.id = :courseId AND c.isPublished = true")
    List<Course> findReviewContent(@Param("courseId") Long courseId, @Param("weakTopics") List<String> weakTopics);

    @Query("SELECT c FROM Course c WHERE c.difficultyLevel = 'ADVANCED' AND c.isPublished = true")
    List<Course> findAdvancedContent(@Param("courseId") Long courseId);

    @Query("SELECT c FROM Course c WHERE c.id > :lastLessonId AND c.isPublished = true")
    List<Course> findNextContent(@Param("courseId") Long courseId, @Param("lastLessonId") Long lastLessonId);

    @Query("SELECT c FROM Course c WHERE c.difficultyLevel = 'ADVANCED' AND c.isPublished = true")
    List<Course> findAdvancedContentForUser(@Param("userId") Long userId, @Param("goalId") Long goalId);
}
