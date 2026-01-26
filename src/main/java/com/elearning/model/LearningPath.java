package com.elearning.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Builder;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "learning_paths")
public class LearningPath {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "goal_id")
    private LearningGoal goal;
    
    @Enumerated(EnumType.STRING)
    private PathStatus status = PathStatus.ACTIVE;
    
    private LocalDate estimatedCompletionDate;
    private LocalDate actualCompletionDate;
    
    @OneToMany(mappedBy = "learningPath", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("sequenceOrder ASC")
    private List<LearningPathCourse> courses = new ArrayList<>();
    
    @CreationTimestamp
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    private LocalDateTime updatedAt;
    
    // Legacy fields for backward compatibility
    @ElementCollection
    private List<Long> recommendedCourseIds = new ArrayList<>();
    
    private String currentFocusArea;
    private Double progress; // Overall progress percentage
    
    public enum PathStatus {
        ACTIVE, COMPLETED, ABANDONED, ON_HOLD
    }
}
