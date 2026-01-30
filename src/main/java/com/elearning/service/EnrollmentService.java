package com.elearning.service;

import com.elearning.dto.EnrollmentDTO;
import com.elearning.dto.CourseAnalyticsDTO;
import com.elearning.model.Enrollment;
import com.elearning.repository.EnrollmentRepository;
import com.elearning.repository.CourseRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class EnrollmentService {

    @Autowired
    private EnrollmentRepository enrollmentRepository;

    @Autowired
    private CourseRepository courseRepository;

    public EnrollmentDTO enrollStudent(Long userId, Long courseId) {
        // Check if already enrolled
        if (enrollmentRepository.existsByUserIdAndCourseId(userId, courseId)) {
            throw new RuntimeException("Student is already enrolled in this course");
        }

        Enrollment enrollment = Enrollment.builder()
                .userId(userId)
                .courseId(courseId)
                .status(Enrollment.EnrollmentStatus.ACTIVE)
                .completionPercentage(java.math.BigDecimal.ZERO)
                .build();

        Enrollment savedEnrollment = enrollmentRepository.save(enrollment);
        return convertToDTO(savedEnrollment);
    }

    public List<EnrollmentDTO> getStudentEnrollments(Long userId) {
        List<Enrollment> enrollments = enrollmentRepository.findByUserId(userId);
        return enrollments.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public CourseAnalyticsDTO getCourseAnalytics(Long courseId, Long instructorId) {
        // Verify instructor owns the course
        // This would need proper implementation
        
        List<Enrollment> enrollments = enrollmentRepository.findByCourseId(courseId);
        
        CourseAnalyticsDTO analytics = new CourseAnalyticsDTO();
        analytics.setCourseId(courseId);
        analytics.setTotalEnrollments(enrollments.size());
        
        long activeCount = enrollments.stream()
                .filter(e -> e.getStatus() == Enrollment.EnrollmentStatus.ACTIVE)
                .count();
        analytics.setActiveEnrollments((int) activeCount);
        
        long completedCount = enrollments.stream()
                .filter(e -> e.getStatus() == Enrollment.EnrollmentStatus.COMPLETED)
                .count();
        analytics.setCompletedEnrollments((int) completedCount);
        
        if (!enrollments.isEmpty()) {
            double avgCompletion = enrollments.stream()
                    .mapToDouble(e -> e.getCompletionPercentage().doubleValue())
                    .average()
                    .orElse(0.0);
            analytics.setAverageCompletionRate(avgCompletion);
        }
        
        return analytics;
    }

    private EnrollmentDTO convertToDTO(Enrollment enrollment) {
        EnrollmentDTO dto = new EnrollmentDTO();
        dto.setId(enrollment.getId());
        dto.setUserId(enrollment.getUserId());
        dto.setCourseId(enrollment.getCourseId());
        dto.setEnrollmentDate(enrollment.getEnrollmentDate());
        dto.setCompletionPercentage(enrollment.getCompletionPercentage());
        dto.setStatus(enrollment.getStatus());
        dto.setLastAccessedAt(enrollment.getLastAccessedAt());
        dto.setCertificateIssued(enrollment.getCertificateIssued());
        return dto;
    }
}
