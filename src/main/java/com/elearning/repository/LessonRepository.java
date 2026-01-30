package com.elearning.repository;

import com.elearning.model.Lesson;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LessonRepository extends JpaRepository<Lesson, Long> {
    List<Lesson> findByModuleIdOrderBySequenceOrder(Long moduleId);
    List<Lesson> findByModuleId(Long moduleId);
    
    @Query("SELECT l FROM Lesson l WHERE l.module.course.id = :courseId AND l.isPreview = true")
    List<Lesson> findPreviewLessonsByCourseId(@Param("courseId") Long courseId);
    
    @Query("SELECT l FROM Lesson l WHERE l.contentType = :contentType")
    List<Lesson> findByContentType(@Param("contentType") Lesson.ContentType contentType);
    
    void deleteByModuleId(Long moduleId);
}
