package com.elearning.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "submissions")
public class Submission {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "assignment_id")
    private Assignment assignment;

    @ManyToOne
    @JoinColumn(name = "student_id")
    private User student;

    private LocalDateTime submittedAt;
    
    @Column(columnDefinition = "TEXT")
    private String content; // Text answer or code
    
    private String fileUrl; // For file uploads
    
    private Integer score;
    private String grade; // A, B, C...
    
    @Column(columnDefinition = "TEXT")
    private String feedback;
    
    private Double plagiarismScore;
    
    private boolean isGraded = false;
}
