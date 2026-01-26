package com.elearning.repository;

import com.elearning.model.Course;
import com.elearning.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface CourseRepository extends JpaRepository<Course, Long> {
    List<Course> findByInstructor(User instructor);
    List<Course> findByCategory(String category);
    List<Course> findByIsPublishedTrue();
}
