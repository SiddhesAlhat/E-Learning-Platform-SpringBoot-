package com.elearning.repository;

import com.elearning.model.LessonContent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LessonContentRepository extends JpaRepository<LessonContent, Long> {
    List<LessonContent> findByLessonId(Long lessonId);
    List<LessonContent> findByContentType(LessonContent.ContentType contentType);
}
