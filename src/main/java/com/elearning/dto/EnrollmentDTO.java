package com.elearning.dto;

import com.elearning.model.Enrollment;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EnrollmentDTO {
    private Long id;
    private Long userId;
    private String username;
    private Long courseId;
    private String courseTitle;
    private String courseThumbnail;
    private LocalDateTime enrollmentDate;
    private BigDecimal completionPercentage;
    private Enrollment.EnrollmentStatus status;
    private LocalDateTime lastAccessedAt;
    private Boolean certificateIssued;
    private String instructorName;
}
