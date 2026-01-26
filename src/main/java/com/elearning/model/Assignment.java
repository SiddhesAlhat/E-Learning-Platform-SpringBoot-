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
@Table(name = "assignments")
public class Assignment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;
    
    @Column(columnDefinition = "TEXT")
    private String description;
    
    private LocalDateTime dueDate;
    private Integer maxScore;

    @OneToOne
    @JoinColumn(name = "lesson_id")
    private Lesson lesson;

    // Configuration for auto-grading
    private boolean autoGrade;
    
    @Column(columnDefinition = "TEXT")
    private String correctAnswer; // For simple auto-grading
    
    private String type; // QUIZ, CODE, UPLOAD

    // For code assignments
    private String language; // java, python, etc.
    @Column(columnDefinition = "TEXT")
    private String testCases; 
}
