package com.elearning.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Builder;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "enrollments")
public class Enrollment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id")
    private Course course;
    
    @CreationTimestamp
    private LocalDateTime enrollmentDate;
    
    @Column(precision = 5, scale = 2)
    private BigDecimal completionPercentage = BigDecimal.ZERO;
    
    @Enumerated(EnumType.STRING)
    private EnrollmentStatus status = EnrollmentStatus.ACTIVE;
    
    @UpdateTimestamp
    private LocalDateTime lastAccessedAt;
    
    private Boolean certificateIssued = false;
    
    public enum EnrollmentStatus {
        ACTIVE, COMPLETED, DROPPED, PAUSED
    }
}
